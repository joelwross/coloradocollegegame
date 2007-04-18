import java.util.*;
import javax.script.*;
public class Resolver
{
	private RuleSet rules;
	private RuleFactory ruleFactory;
	private RepresentationResolver repResolver = null;
	private ActionFactory actionFactory;
	private HashMap<Integer,GameElement> elementsHash;
	private ScriptEngineManager manager;
	private World world;
	private IO io;
	private Logger myLogger;

	public Resolver(World _world, RuleFactory _rf, ActionFactory _af, RepresentationResolver _repResolver, Logger _myLogger)
	{
		this(_world, _rf, _af, _myLogger);
		repResolver = _repResolver;
	}
	public Resolver(World _world, RuleFactory _rf, ActionFactory _af, Logger _myLogger)
	{
		ruleFactory = _rf;
		actionFactory = _af;
		rules = _rf.getRuleSet("");
		elementsHash = _world.getElementsHash();
		manager = new ScriptEngineManager();
		world = _world;
		myLogger = _myLogger;

		// Add logger at global scope
		manager.put("myLogger", myLogger);

		// Add static stuff at global scope
		manager.put("Constants", new Constants());
		manager.put("Quaternions", new Quaternions());
		manager.put("VectorUtils", new VectorUtils());
	}

	public void setIO(IO _io)
	{
		io = _io;
	}

	public int parseOld(Object _message)
	{
		if(_message instanceof IncrementedArray)
		{
			parse(_message);
			return Constants.SUCCESS;
		}

		Object[] message = (Object[]) _message;

		switch(((Integer) message[0]).intValue())
		{
			case Constants.MOVE_TO:
				world.setPosition( message, 1);
				break;
			case Constants.ROTATE_TO:
				world.setFacing( message, 1);
				break;
			case Constants.ATTRIBUTE:
				world.setAttribute( message, 1);
				break;
			case Constants.ADD_PLAYER:
				world.addElement( message, 1);
				break;
			case Constants.REMOVE_PLAYER:
				world.removeElement( message, 1 );
				myLogger.message("Removing player: " + message[1] + "\n", false);
				break;
			case Constants.SEND_WORLD:
				world.addMultipleElements( message, 1);
				break;
			default:
				myLogger.message("Received unparsable message: " + message[0] + "\n", true);
		}

		return Constants.SUCCESS; //eventually every action in the world will return an int for whether or not it was a valid action
	}

	@SuppressWarnings("unchecked")
	public int[] parse(Object _actions)
	{
		if(!(_actions instanceof IncrementedArray))
		{
			myLogger.message("Resolver parse received a bad message, ignoring: " + _actions + "\n", true);
			return null;
		}

		IncrementedArray<WritableAction> actions = (IncrementedArray<WritableAction>) _actions;
		int[] results = new int[actions.length];

		for(int i = 0; i < actions.length; i++)
			results[i] = parse(actions.get(i).getAction(world,actionFactory));

		return results;
	}

	public int parse(Action action)
	{
		GameElement[] nouns = action.getNouns();

		int[] needles = new int[Constants.SENTENCE_LENGTH];
		needles[0] = actionFactory.getType(action.getName());
		myLogger.message("Resolver parsing with needles: " + needles[0] + ",", false);
		if(nouns != null)
		{
			for(int i = nouns.length-1; i >= 0; i--)
			{
				needles[i+1] = nouns[i].getTypeId();
				myLogger.message(needles[i+1]+",",false);
			}
		}
		myLogger.message("\n", false);

		Rule[] applicable = rules.getRules(needles);
		IncrementedArray<GameElement> relevantElements = new IncrementedArray<GameElement>(Constants.DEFAULT_RELEVANT_SIZE);
		if(nouns != null)
		{
			GameElement subject = nouns[0];
			GameElement first = world.getFirstElement();
			GameElement currentElement = first;
			do
			{
				/*find relevent ojects and get rules that apply to this sentence*/
				if(currentElement != subject && subject.isRelevant(currentElement))
					relevantElements.add(currentElement);
			}
			while( (currentElement=currentElement.next) != first );
		}


		return resolve(applicable,action,relevantElements);
	}

	@SuppressWarnings("fallthrough")
	public int resolve( Rule[] _rules, Action _action, IncrementedArray<GameElement> _relevantElements)
	{
		Object[] message = _action.parameters().pack();
		GameElement[] nouns = _action.getNouns();
		GameElement subject = null, directObject = null, indirectObject = null;
		GameElement[] other = null;
		int status = Constants.SUCCESS;
		ActionsHashMap myActions = new ActionsHashMap(actionFactory); 
		ActionsHashMap actionsToSend = new ActionsHashMap(actionFactory);
		
		if(nouns != null)
		{
			switch(nouns.length)
			{
				default:
					other = new GameElement[nouns.length-3];
					System.arraycopy(nouns,3,other,0,other.length);
				case 3:
					indirectObject = nouns[2];
				case 2:
					directObject = nouns[1];
				case 1:
					subject = nouns[0];
					break;
			}
		}

		myActions.add(_action);

		for(Rule rule : _rules)
		{
			try {
				ScriptEngine engine = manager.getEngineByName(rule.language());
				engine.put("owner",rule.owner());
				engine.put("subject",subject);
				engine.put("directObject",directObject);
				engine.put("indirectObject",indirectObject);
				engine.put("other",other);
				engine.put("relevant",_relevantElements);
				engine.put("argv",message);
				engine.put("myActions",myActions);
				engine.put("actionsToSend",actionsToSend);
				engine.eval(rule.function());
				Object _status = engine.get("status");
				if(_status instanceof Integer)
					status = ((Integer) _status).intValue();
				else if(_status instanceof Double)
					status = ((Double) _status).intValue();
				if(status != Constants.SUCCESS)
					return status;
			}
			catch(ScriptException se)
			{
				myLogger.message("Script error: " + se.getMessage() + "\n",true);
			}

		}

		myLogger.message("Resolver is starting to handle actions...\n", false);
		IncrementedArray<Action> actionList = myActions.getAll();
		for(int i = 0; i < actionList.length; i++)
		{
			_action = actionList.get(i);
			if(_action.getWorldFunction() != null)
			{
				nouns = _action.getNouns();
				if(nouns != null)
				{
					switch(nouns.length)
					{
						default:
							other = new GameElement[nouns.length-3];
							System.arraycopy(nouns,3,other,0,other.length);
						case 3:
							indirectObject = nouns[2];
						case 2:
							directObject = nouns[1];
						case 1:
							subject = nouns[0];
							break;
					}
				}

				StringFunction worldFunc = _action.getWorldFunction();
				try {
					ScriptEngine engine = manager.getEngineByName(worldFunc.getLanguage());
					engine.put("subject",subject);
					engine.put("directObject",directObject);
					engine.put("indirectObject",indirectObject);
					engine.put("other",other);
					engine.put("argv",_action.parameters().pack());
					engine.eval(worldFunc.getFunction());
				}
				catch(ScriptException se)
				{
					myLogger.message("Script error: " + se.getMessage() + "\n",true);
				}

			}

			if(_action.getRepFunction() != null && repResolver != null)
			{
				repResolver.resolve(_action);
			}
		}

		synchronized(world.getFirstElement())
		{
			world.getFirstElement().notifyAll();
		}
		if(actionsToSend.size() > 0)
		{
			IncrementedArray<Action> act = actionsToSend.getAll();
			IncrementedArray<WritableAction> write_act = new IncrementedArray<WritableAction>(act.length);
			for(int i = 0; i < act.length; i++)
			{
				write_act.add(new WritableAction(act.get(i)));
			}
			io.send(write_act);
		}

		return Constants.SUCCESS;
	}
}

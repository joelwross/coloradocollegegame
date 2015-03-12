# Introduction #

A copy/paste job of an e-mail sent by Omer Bar-or to Joel Ross to clear up how the rule/action-handling section of the code works.

# Details #

The Resolver is a thread that has a stack of actions to process. It pops an action from the top of the stack and parses it. "Parse" here means that it grabs all of the rules associated with that action, whether associated with the verb (the name of the action), the subject (the person calling the action), the direct object (the target of the action), or the indirect object (the object associated with the action). It piles all of those rules into an array. Then, it grabs all relevant objects associated with the subject of the action (if there is one ["add element" doesn't have a subject, for example]) and stores them as well. Finally, it passes the action, all of the rules, and all of the relevant objects on to the "resolve" function. The "resolve" function calls each rule in succession. If any of those rules fail, the action fails, and the "resolve" function returns without doing anything else. (So, if you try to walk into a wall, no matter how many other things might be caused by that movement if it were to succeed [bomb goes off, you lose hitpoints because you're poisoned, &c](a.md), the action fails, so none of them actually happen.) If any rule dictates a new dependency (like, if you push someone, you will only move if she or he can also move), that dependency is added to the Action, and at the end of the rules-processing, the Action is moved back to the top of the stack to be reprocessed. (Note: this last bit about dependencies is still untested.) Then, the next time it is parsed, not only is the action parsed, but so are all of its dependencies, such that when the "resolve" function checks for rules, it checks all of the rules before allowing any action to be called.

If no rule claims to have created a new dependency or to have failed, all actions are called (i.e., the action that just got parsed and all of its dependencies), and these actions change the GameElements themselves. Processing actions takes place in two phases. First, the "inworld" function is processed to make the changes in our logical world ( i.e., World.java), and then, the "inrepresentation" function is processed to let the Representation know what has changed. (This last part is actually handled by the RepresentationResolver's "resolve" function, which receives as input the Action object to be Resolved.) Then, if the Action object being processed has a "toSend" Action stored in it, that variable is added to an array of elements to be sent along (either to the server, if this is the client's Resolver, or to the client, if this is the Server's resolver). If the action still has a duration, that action is pushed back onto the end of the stack to be processed again later (still untested). And, finally, the "toSend" variable is sent using the IO's "send" function.

What the Action Object contains:
  * A name (e.g., "move")
  * An ID (for doing the "parse" a bit more quickly, otherwise unused)
  * A worldFunction (that gets called by the Resolver, see above)
  * A representationFunction (that gets called by the Resolver, see above)
  * An Object array (actually an Object IncrementedArray) of parameters that affect this action
  * A GameElement array of nouns associated with this action ( i.e., subject, direct object, indirect object)
  * A toSend Action (that gets sent onto the Server/Client if this Action is called)
  * A delay (for when to next call the action -- currently untested)
  * A lastRun time ( i.e., the last time the action was run, used for when to next call the action -- currently untested)
  * An IncrementedArray of type Action of dependent Action objects (for dependencies, see above)

What a rule receives with which to work (the variable name is the word in quotes):
  * "myLogger" -- the Logger object
  * "Constants"
  * "Quaternions"
  * "VectorUtils"
  * The "GameElement" class (not object... this is used for determining if a parameter is of type GameElement)
  * The "actionStack" (the stack of Actions from which the Resolver works)
  * The "actionFactory" (for making new Actions)
  * The "action" Object that this rule is processing (so that the rule can change its parameters and add a "toSend" to it)
  * The "owner" of the rule (currently unimplemented, but will be for rules that apply to only a single GameElement)
  * The "subject" of the action (i.e., the 0th element of the action's "nouns" array)
  * The "directObject" of the action (i.e., the 1st element of the action's "nouns" array)
  * The "indirectObject" of the action (i.e., the 2nd element of the action's "nouns" array)
  * Any "other" objects associated with the action (i.e., all of the elements in the action's "nouns" array starting with the 3rd -- should probably never be used)
  * All "relevant" objects as determined by the Resolver's parse method (see above)
  * The action's parameters (variable name: "argv")
  * The "status" of the action - this is the variable that gets changed to communicate a new dependency or if the rule failed.

What a worldFunction of an action receives with which to work (the variable name is the word in quotes):
  * "myLogger" -- the Logger object
  * "Constants"
  * "Quaternions"
  * "VectorUtils"
  * The "GameElement" class (not object... this is used for determining if a parameter is of type GameElement)
  * The "actionStack" (the stack of Actions from which the Resolver works)
  * The "actionFactory" (for making new Actions)
  * The "subject" of the action (i.e., the 0th element of the action's "nouns" array)
  * The "directObject" of the action (i.e., the 1st element of the action's "nouns" array)
  * The "indirectObject" of the action (i.e., the 2nd element of the action's "nouns" array)
  * Any "other" objects associated with the action (i.e., all of the elements in the action's "nouns" array starting with the 3rd -- should probably never be used)
  * The action's parameters (variable name: "argv")
  * The "status" of the action - this is the variable that gets changed to communicate any special returns for this action (see the next object below).
  * An empty Object called "returnVals" that lets the action return an Object to the Resolver (currently used for adding and removing elements, since those are functions in World.java -- basically, to add an element, the action sets its "returnVals" to that GameElement and then sets its status to "Constants.ADD\_ELEMENTS"

PLEASE NOTE: The worldFunction of action receives exactly the same parameters as the rule does, both coming from the "parameters" object in an Action (so, to change "argv" for an action, it might even be enough to just change "argv" in the rule, but to be safe (scripting within Java, I've discovered, is incredibly finicky about types), I would  change the parameter variable in the Action object _to be_ the argv with which you work in your rule (with this line: `action.parameters(argv);`)).


What a representationFunction of an action receives with which to work (the variable name is the word in quotes):
  * "myLogger" -- the Logger object
  * "Constants"
  * "Quaternions"
  * "VectorUtils"
  * "representation" -- the Representation that needs to be notified of changes
  * The "subject" of the action (i.e., the 0th element of the action's "nouns" array)
  * The "directObject" of the action (i.e., the 1st element of the action's "nouns" array)
  * The "indirectObject" of the action (i.e., the 2nd element of the action's "nouns" array)
  * Any "other" objects associated with the action (i.e., all of the elements in the action's "nouns" array starting with the 3rd -- should probably never be used)
  * The action's parameters (variable name: "argv")


To create a new action, all you really need to do is create a new 

&lt;action&gt;

 in a .ccr file and give it a unique name. To apply rules to that action, you just create a new 

&lt;rule&gt;

 with `<verb>{Your action name}</verb>` and that will make it apply to your action. You can also make a rule that applies to a particular type of GameElement as its subject with `<subject>{GameElement's type}</subject>`, or a rule that applies to a specific action and subject with:
```
<verb>{Your action name}</verb>
<subject>{GameElement's type}</subject>
```
Rules can also apply to directObjects and indirectObjects, and to any combination of them. If you include multiple verbs/subjects/&c., a new rule will be created for each combination, so:
```
<verb>action1</verb>
<verb>action2</verb>
<subject>ge_type</subject>
```
Will create two new rules, one associated with ge\_type/action1 and one associated with ge\_type/action2.


... I think that's it. Let me know if there's anything else that doesn't make sense.




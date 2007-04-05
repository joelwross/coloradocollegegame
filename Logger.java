public class Logger
{
	private boolean verbose;
	
	public Logger(boolean _v)
	{
		verbose = _v;
	}

	public void message(String _m, boolean error)
	{
		if(error)
		{
			System.err.print(_m);
		}
		else if(verbose)
		{
			System.out.print(_m);
		}
	}

}

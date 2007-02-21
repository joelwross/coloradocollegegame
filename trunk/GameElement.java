import java.util.*;
public class GameElement
{ 
	int status;
	boolean isClient;

	int[] position = new int[3]; // Absolute postition of the element
	VirtualShape[] shapes = null;
	HashMap attributes = null;
	String type = null;
	int[] scale = new int[3];

	// Remove these once we have Element and ElementGenerator working
	public int[][] dimensions = new int[3][4]; 

	public GameElement( String _type, int[] _position, int[] _scale, VirtualShape[] _shapes, HashMap _attributes)
	{
		type = _type;
		position = _position;
		shapes = _shapes;
		scale = _scale;
		attributes = _attributes;

		System.out.println("New element: " + type + ", has: " + shapes.length + " shapes");
	}

	public GameElement( int _x , int _y , int _z , int _width, int _length )
	{
		initialize( _x , _y , _z , _width , _length );
	}

	public GameElement(  int _x , int _y , int _z , int _width, int _length , int _status )
	{
		initialize( _x , _y , _z , _width , _length );

		status = _status;
	}

	public void initialize( int _x , int _y , int _z , int _width, int _length )
	{
		isClient = false;

		position[X] = _x;
		position[Y] = _y;
		position[Z] = _z;
		
		dimensions[X][0] = -(int)(_width/2);
		dimensions[X][1] = (int)(_width/2);
		dimensions[X][2] = (int)(_width/2);
		dimensions[X][3] = -(int)(_width/2);

		dimensions[Y][0] = -(int)(_length/2);
		dimensions[Y][1] = -(int)(_length/2);
		dimensions[Y][2] = (int)(_length/2);
		dimensions[Y][3] = (int)(_length/2);

		//am not worring about third dimension yet
		dimensions[Z][0] = 0;
		dimensions[Z][1] = 0;
		dimensions[Z][2] = 0;
		dimensions[Z][3] = 0;
	}

	public void toggleIsClient()
	{
		isClient = !isClient;
	}

	public void nudge(int _dx, int _dy, int _dz)
	{
		position[X] += _dx;
		position[Y] += _dy;
		position[Z] += _dz;
	} // nudge

	public void nudge( int[] delta )
	{
		for( int i = 0 ; i < delta.length; i++ )
			position[i] += delta[i];
	}

	public void setPosition(int _x, int _y, int _z)
	{
		position[X] = _x;
		position[Y] = _y;
		position[Z] = _z;
	} // setPosition
	
	public int[] getPosition()
	{
		return position;
	}

	public int getPosition(int dim)
	{
		return position[dim];
	}

	public void rotate(double _theta)
	{
		// So far emtpy... fill me please
	}

	public int[][] getAbsoluteCoordinates()
	{
		for( int i = 0; i < dimensions[X].length ; i++ )
		{
			absDimensions[X][i] = position[X]+dimensions[X][i];
			absDimensions[Y][i] = position[Y]+dimensions[Y][i];
			absDimensions[Z][i] = position[Z]+dimensions[Z][i];
		}
		
		return absDimensions;
	}
	
	public int[] getInfoArray()
	{
		return new int[] { position[X], position[Y] , position[Z] , status };
	}

	public GameElement clone()
	{
		try {
			return (GameElement) super.clone();
		}
		catch(CloneNotSupportedException cnse)
		{
				return null;
		}
	}

	int[][] absDimensions = new int[3][4];

	public final static int X=0, Y=1, Z=2;
} // class GameElement

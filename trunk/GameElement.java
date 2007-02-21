import java.util.*;
public class GameElement
{ 
	private int id;
	int typeId;
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
		initialize(position, 10, 10);
		System.out.println("New element: " + type + ", has: " + shapes.length + " shapes");
	}

	public GameElement( GameElement original )
	{
		type = new String(original.type);
		shapes = new VirtualShape[original.shapes.length];
		scale = new int[original.scale.length];
		System.arraycopy(original.position,0,position,0,original.position.length);
		System.arraycopy(original.shapes,0,shapes,0,original.shapes.length);
		System.arraycopy(original.scale,0,scale,0,original.scale.length);
		if(original.attributes != null)
			attributes = (HashMap) original.attributes.clone();
		typeId = original.typeId;
		initialize(position, 10, 10);
	}

	public void initialize( int[] _position , int _width, int _length )
	{
		isClient = false;

		int _x = _position[0];
		int _y = _position[1];
		int _z = _position[2];
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

	public boolean isClient()
	{
		return isClient;
	}

	public void setTypeId(int _id)
	{
		typeId = _id;
	}

	public int getTypeId()
	{
		return(typeId);
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
		return new int[] { typeId, position[X], position[Y] , position[Z] , status };
	}

	int[][] absDimensions = new int[3][4];

	public final static int X=0, Y=1, Z=2;
} // class GameElement

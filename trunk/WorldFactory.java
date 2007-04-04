import java.util.*;
import java.io.File;
import java.io.FileFilter;
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class WorldFactory
{
	private Logger myLogger;
	private File[] files;
	private ElementFactory ef;

	public WorldFactory(File folder, String ext, ElementFactory _ef, Logger _myLogger)
	{
		ef = _ef;
		myLogger = _myLogger;
		files = folder.listFiles(new WGFileFilter(ext));
	}

	public WorldFactory(File folder, ElementFactory _ef, Logger _myLogger)
	{
		ef = _ef;
		myLogger = _myLogger;
		files = folder.listFiles(new WGFileFilter(Constants.WORLD_EXTENSION));
	}

	public WorldFactory(ElementFactory _ef, Logger _myLogger)
	{
		ef = _ef;
		myLogger = _myLogger;
		File folder = new File(Constants.DEFAULT_DATA_DIR);
		files = folder.listFiles(new WGFileFilter(Constants.WORLD_EXTENSION));
	}

	public void fillWorld(World w)
	{
		try
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc;
			Element element, attributesElement, newAttribute;
			NodeList elements, positionNodes, facingNodes, scaleNodes, attributesNodes;
			Node tmpNode;
			String attributeType;
			GameElement newElement;
			float[] position;
			float[] facing;
			float[] scale;
			int i;
			
			for(File file : files)
			{
				doc = db.parse(file);
				elements = doc.getElementsByTagName("element");
				for(i = elements.getLength()-1;i>=0; i--)
				{
					element = (Element) elements.item(i);

					newElement = ef.getGameElement(element.getAttribute("type"));
					newElement.id(i);					

					positionNodes = element.getElementsByTagName("position");
					position = new float[positionNodes.getLength()];
					for(int p = position.length-1; p>=0; p--)
					{
						position[p] = Float.parseFloat(positionNodes.item(p).getTextContent());
					}
					newElement.setPosition(position);
					
					facingNodes = element.getElementsByTagName("facing");
					if(facingNodes.getLength() == 3)
					{
						float[] facingEuler = new float[3];
						for(int f = facingEuler.length-1; f>=0; f--)
						{
							facingEuler[f] = Float.parseFloat(facingNodes.item(f).getTextContent());
							facingEuler[f] = (float)Math.toRadians(facingEuler[f]); //change to Radians. We like radians.
						}
						facing = Quaternions.getQuatFromEuler(facingEuler); //set the rotation to be in Quaternions
					}
					else //if we didn't have a 3D rotation specified
						facing = Constants.DEFAULT_FACING; //set to the default unit
					newElement.setFacing(facing);

					scaleNodes = element.getElementsByTagName("scale");
					if(scaleNodes.getLength() != 0)
					{
						scale = new float[scaleNodes.getLength()];
						for(int s = scale.length-1; s>=0; s--)
						{
							scale[s] = Float.parseFloat(scaleNodes.item(s).getTextContent());
						}
						newElement.setScale(scale);
					}	
					
					attributesElement = (Element) element.getElementsByTagName("attributes").item(0);
					if(attributesElement != null)
					{
						// grab all of the children of attributesElement
						attributesNodes = attributesElement.getChildNodes();

						for(int a = attributesNodes.getLength()-1; a>=0; a--)
						{
							tmpNode = attributesNodes.item(a);
							if(tmpNode.getNodeType() != Node.ELEMENT_NODE) continue;
							newAttribute = (Element) tmpNode;
							attributeType = newAttribute.getAttribute("type");
							if(attributeType.equalsIgnoreCase("String"))
							{
								newElement.attribute(newAttribute.getTagName(), newAttribute.getTextContent());
							}
							else if(attributeType.equalsIgnoreCase("int"))
							{
								newElement.attribute(newAttribute.getTagName(), Integer.parseInt(newAttribute.getTextContent()));
							}
							else if(attributeType.equalsIgnoreCase("float"))
							{
								newElement.attribute(newAttribute.getTagName(), Float.parseFloat(newAttribute.getTextContent()));
							}
							else if(attributeType.equalsIgnoreCase("hex32")) //for 32bit hexadecimal
							{
								newElement.attribute(newAttribute.getTagName(), (int)Long.parseLong(newAttribute.getTextContent(),16));	
							}
							// &c.
						}
					}
					
					// add element to World
					w.addElement(newElement);
				}
			}
			
		}
		catch(Exception e) // better error reporting!
		{
			myLogger.message("Failed to read input files! : " + e.getMessage() + " :\n", true);
			e.printStackTrace();
		}

	}



	private class WGFileFilter implements FileFilter
	{
		private String ext;
		public WGFileFilter(String _ext)
		{
			ext = _ext;
		}

		public boolean accept(File pathname)
		{
			return(pathname.getName().endsWith(ext));
		}
	}
}
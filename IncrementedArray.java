import java.io.Serializable;

public class IncrementedArray<T> implements Serializable
{
	static final long serialVersionUID = 2368403357597384006L;
	private int incrementSize;
	private T[] data;
	public int length;

	@SuppressWarnings("unchecked")
	public IncrementedArray(int _incrementSize)
	{
		data = (T[]) new Object[_incrementSize];
		length = 0;
		incrementSize = _incrementSize;
	}

	@SuppressWarnings("unchecked")
	public IncrementedArray(T[] _input, int _incrementSize)
	{
		if(_input == null)
		{
			data = (T[]) new Object[_incrementSize];
			length = 0;
		}
		else
		{
			data = _input;
			length = _input.length;
		}
		incrementSize = _incrementSize;
	}

	public T[] get()
	{
		return data;
	}
	public T get(int id)
	{
		return data[id];
	}

	@SuppressWarnings("unchecked")
	public void add(T _datum)
	{
		if(length == data.length)
		{
			T[] tmp = (T[]) new Object[length+incrementSize];
			System.arraycopy(data,0,tmp,0,length);
			data = tmp;
		}
		data[length++] = _datum;
	}

	@SuppressWarnings("unchecked")
	public void add(IncrementedArray<T> _data)
	{
		if(length+_data.length >= data.length)
		{
			T[] tmp = (T[]) new Object[length+_data.length];
			System.arraycopy(data,0,tmp,0,length);
			data = tmp;
		}
		System.arraycopy(_data.data,0,data,length,_data.length);
		length += _data.length;
	}

	@SuppressWarnings("unchecked")
	public T[] pack()
	{
		T[] tmp = (T[]) new Object[length];
		System.arraycopy(data,0,tmp,0,length);
		data = tmp;
		return data;
	}
}

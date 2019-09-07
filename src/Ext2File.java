import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Ext2File
{
	/**
	 * @param file - the file that is being read from
	 * @param byteBuffer - the buffer to hold the byte(s) read from the file
	 */
	public int seekPos = 0; 
	public RandomAccessFile file;
	public ByteBuffer byteBuffer;
	
	/**
	 * 
	 * @param blockNum - the block number in the filesystem
	 * @param offset - the amount in bytes to offset from the block
	 * @param length - the number of bytes to read
	 * @return byteBuffer - returns byteBuffer with requested bytes read
	 * Using math, calculate the number of bytes to start reading from as each
	 * block is 1024 bytes long.
	 */
	public ByteBuffer readBlock(int blockNum, int offset, int length)
	{
		return readFile((blockNum * 1024) + offset, length);
	}
	
	/**
	 * 
	 * @param byteToReadFrom - the number to move the seeker to and start reading bytes from
	 * @param length - the number of bytes to read
	 * @return byteBuffer - returns byteBuffer with requested bytes read
	 * Create a byte array which is the same length of the number of bytes to read,
	 * read at the specified position into the array, wrap it in a buffer, and return it. 
	 */
	public ByteBuffer readFile(int byteToReadFrom, int length)
	{
		byte[] byteArray = new byte[(int)length];
		
		try
		{
			file.seek(byteToReadFrom);
			file.read(byteArray);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
						
		byteBuffer = ByteBuffer.wrap(byteArray);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		
		return byteBuffer;
	}
	
	/**
	 * Terminates the current session by closing the file and stopping the shell
	 */
	public void terminate()
	{
		try
		{
			file.close();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	/**
	 * @param length - the number of bytes to read from the current seek position
	 * readSeek will read a number of bytes specified by length from the files pointer
	 * @return 
	 */
	public ByteBuffer readSeek(int length)
	{
		try
		{
			return readFile((int) file.getFilePointer(), length);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return readFile(0,0);
	}
	
	/**
	 * @return filePointer - the position of the pointer
	 * getPosition will return the position of the file pointer
	 */
	public long getPosition()
	{
		long filePointer = 0;
		
		try
		{
			filePointer = file.getFilePointer();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return filePointer;
	}
}

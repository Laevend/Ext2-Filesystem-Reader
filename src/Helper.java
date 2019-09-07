import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class Helper
{
	/**
	 * 
	 * @param byteBuf - the buffer of bytes
	 * 
	 * @param byteA - the byte array that the byte buffer is unpacked into
	 * @param numOfLoops - the calculated number of loops to run until there are no more bytes to read
	 * @param byteCount - a number to offset the reading point in the byte array
	 * 
	 * Takes a byte buffer and dumps hex on the screen
	 */
	public void readHex(ByteBuffer byteBuf)
	{
		byte[] byteA = byteBuf.array();
		int numOfLoops = (int)Math.ceil((double)byteA.length / 16.0);
		int byteCount = 0;
		
		for(int i = 0; i < numOfLoops; i++)
		{
			for(int j = 0; j < 16; j++)
			{
				if(j == 8)
				{
					System.out.print("| ");
				}
				if(j < byteA.length)
				{
					try
					{
						System.out.print(String.format("%02X",byteA[j + byteCount]) + " ");
					}
					catch(ArrayIndexOutOfBoundsException e)
					{
						System.out.print("XX ");
					}
				}
				else
				{
					System.out.print("XX ");
				}
				
			}			
			System.out.print("| ");
			
			for(int j = 0; j < 16; j++)
			{
				byte[] charByte = new byte[1];
				String singleChar;
				
				try
				{
					charByte[0] = byteA[j + byteCount];
				}
				catch(ArrayIndexOutOfBoundsException e)
				{
					singleChar = ".";
				}
				
				try
				{
					singleChar = new String(charByte, "UTF-8");
				} catch (UnsupportedEncodingException e)
				{
					singleChar = ".";
				}
				
				if(singleChar.trim().length() == 0)
				{
					singleChar = ".";
				}
				
				if(j == 8)
				{
					System.out.print("| ");
				}
				if(j < byteA.length)
				{
					System.out.print(singleChar + " ");
				}
				else
				{
					System.out.print("_ ");
				}
			}
			byteCount += 16;
			System.out.println("");
		}
	}
}

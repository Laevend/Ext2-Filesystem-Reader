import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Volume
{	
	/**
	 * @param hlp - an instance of Helper
	 * @param ext - an instance of Ext2File
	 * 
	 * @param blockGroups - Holds the total number of block groups in the file system
	 * 
	 * @param totalBytes - Holds the total number of bytes in the file system
	 * @param totalInodes - Holds the total number of inodes in the file system
	 * @param totalBlocks - Holds the totnal number of blocks in the file system
	 * @param blocksPerGroup - Holds the number of blocks per group
	 * @param inodesPerGroup - Holds the number of inodes per group
	 * @param sizeOfInodes - Holds the size of each inode in the file system
	 * 
	 * @param inodeTablePoint[] - Holds an array of inode table pointers
	 * @param fileSize - Holds the size of a file (in bytes)
	 * @param dataBlockPoint - Holds a data block pointer
	 * 
	 * @param currentDirect - Holds the current directory the user is in (via inode)
	 * @param nextDirect - Holds the directory to be naviagted to
	 * @param countDirect - Holds the number of directory navigations executed in one execution
	 */
	Helper hlp = new Helper();
	Ext2File ext = new Ext2File();
	
	public int blockGroups;
	
	public long totalBytes;
	public int totalInodes;
	public int totalBlocks;
	public int blocksPerGroup;
	public int inodesPerGroup;
	public int sizeOfInodes;
	
	public int inodeTablePoint[];
	public long fileSize;
	public int dataBlockPoint;
	
	public int currentDirect = 2;
	public int nextDirect = 0;
	public int countDirect = 0;
	
	/**
	 * 
	 * @param filePath - the path of the ext2 file to be read
	 * Volume receives a string of the file path and opens it.
	 * it automatically reads the superblock to set up the variables so
	 * that the filesystem can be read correctly.
	 */
	public Volume(String filePath)
	{
		try
		{
			ext.file = new RandomAccessFile(filePath,"r");
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		readSuperBlock();
	}
	
	/**
	 * readSuperBlock will read the superblock from block 2 (1024 bytes) in the filesystem,
	 * in addition to the inode table pointers from the group descriptor
	 */
	public void readSuperBlock()
	{
		System.out.println(">> Reading Superblock...");
		System.out.println("");
		
		try
		{
			totalBytes = (int) ext.file.length();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		
		totalInodes = ext.readBlock(1,0,4).getInt();
		totalBlocks = ext.readBlock(1,4,4).getInt();
		blocksPerGroup = ext.readBlock(1,32,4).getInt();
		inodesPerGroup = ext.readBlock(1,40,4).getInt();
		sizeOfInodes = ext.readBlock(1,88,4).getInt();
		blockGroups = (int) Math.ceil((double) totalBlocks / (double) blocksPerGroup);
		inodeTablePoint = new int[blockGroups];
		
		System.out.println(" > Total number of bytes: " + totalBytes);
		System.out.println(" > Magic number: " + String.format("Ox%04X",ext.readBlock(1,56,2).getShort()));
		System.out.println(" > Total number of inodes: " + totalInodes);
		System.out.println(" > Total number of blocks: " + totalBlocks);
		System.out.println(" > Number of blocks per group: " + blocksPerGroup);
		System.out.println(" > Number of inodes per group: " + inodesPerGroup);
		System.out.println(" > Size of each inodes (bytes): " + sizeOfInodes);
		System.out.format(" > Volume label: " + "%s\n", new String(ext.readBlock(1,120,16).array()));
		System.out.println("");
		
		System.out.println(">> Reading GroupDesc...");
		System.out.println("");
		
		for(int i = 0; i < blockGroups; i++)
		{
			inodeTablePoint[i] = ext.readBlock(2,(i * 32) + 8,4).getInt();
			System.out.println(" > Inode table pointer " +  i + ": " + inodeTablePoint[i]);
		}
	}
	
	/**
	 * @param inode - the inode to read from
	 * displayIno will take an inode and read information about that inode.
	 * The contents of the inode is displayed in data block pointers which
	 * point to a block of data that can be read
	 */
	public void displayIno(int inode)
	{		
		boolean pointerExists = false;
		
		System.out.format(">> Reading Inode: " + inode + "...\n");
		System.out.println(" > Filemode: " + convertFileMode(readInode(inode,0,2).getShort()));
		System.out.println(" > User ID: " + readInode(inode,2,2).getShort()); // check with hex to confirm if all the bits where there
		System.out.println(" > Last access: " + new Date(readInode(inode,8,4).getInt() * 1000L));
		System.out.println(" > Creation: " + new Date(readInode(inode,12,4).getInt() * 1000L));
		System.out.println(" > Last modified: " + new Date(readInode(inode,16,4).getInt() * 1000L));
		System.out.println(" > Deleted: " + new Date(readInode(inode,20,4).getInt() * 1000L));
		System.out.println(" > Group ID: " + readInode(inode,2,2).getShort());
		System.out.format(" > Number of hard links: " + readInode(inode,2,2).getShort() + "\n");
		
		for(int i = 0; i < 12; i ++)
		{
			dataBlockPoint = readInode(inode,(i * 4) + 40,4).getInt();
			if(Driver.getIsEmpty())
			{
				System.out.println(" > Data block pointer " + (i + 1) + "/12: " + readInode(inode,(i * 4) + 40,4).getInt());
			}
			else
			{
				if(dataBlockPoint != 0)
				{
					System.out.println(" > Data block pointer " + (i + 1) + "/12: " + readInode(inode,(i * 4) + 40,4).getInt());
					pointerExists = true;
				}
			}
		}
		if(!pointerExists)
		{
			System.out.println(" > No data pointers found ");
		}
		
		System.out.println("");
		System.out.println(" > Indirect pointer: " + readInode(inode,88,4).getInt());
		System.out.println(" > Double indirect pointer: " + readInode(inode,92,4).getInt());
		System.out.println(" > Triple indirect pointer: " + readInode(inode,96,4).getInt());
		System.out.println(" > Filesize: " + getSizeOfFile(inode).getInt());
	}
	
	/**
	 * 
	 * @param ino - the inode to be read
	 * @param offset - the offset in bytes to read from
	 * @param bytesToRead - the number of bytes to read from the inode
	 * @return byteBuffer - returns byteBuffer with requested bytes read
	 * readInode will take an inode number, along with it's offset and length,
	 * and calculate it's byte position in the filesystem to read from
	 */
	public ByteBuffer readInode(int ino, int offset, int bytesToRead)
	{
		int inoGroup = (ino - 1) / inodesPerGroup;
		int inoIndex = (ino - 1) % inodesPerGroup;
		
		if(inoGroup <= blockGroups)
		{
			int inoTabPos = inodeTablePoint[inoGroup] * 1024; //block size
			int inoPos = inoTabPos + (inoIndex * sizeOfInodes);
			
			return ext.readFile(inoPos + offset, bytesToRead);
		}
		else
		{
			System.out.println(">> The value entered can not be read as an inode");
			return ext.readFile(0,0);
		}		
	}	
	/**
	 * 
	 * @param dataNum - the inode or blocknumber to be read
	 * @param dataType - the type of data, 0 is an inode, 1 is a block number
	 * readTxt will take an inode or block number and read it as a normal text file by reading
	 * the block as a string
	 */
	public void readTxt(int dataNum, int dataType)
	{
		String fileString = "";
		switch(dataType)
		{
			case 0: // inode data type
			{
				for(int i = 40; i < 88; i += 4)
				{
					dataBlockPoint = readInode(dataNum,i,4).getInt();
					if(dataBlockPoint > 0)
					{
						try
						{
							fileString +=  new String(ext.readBlock(dataBlockPoint,0,1024).array(), "UTF-8");
						} catch (UnsupportedEncodingException e)
						{
							e.printStackTrace();
						}			
					}
				}
				System.out.println(fileString.trim());
				break;
			}
			case 1: // block data type
			{
				dataBlockPoint = dataNum;
				
				try
				{
					fileString +=  new String(ext.readBlock(dataBlockPoint,0,1024).array(), "UTF-8");
					System.out.println(fileString.trim());
				} catch (UnsupportedEncodingException e)
				{
					e.printStackTrace();
				}
				break;
			}
		}
	}
	
	/**
	 * 
	 * @param blockNum - the block number to read
	 * 
	 * @param fileNameLen - the length of the file name
	 * @param length - the length of the inode
	 * @param blockPos - the offset to read from
	 * @param traverseBlock - A counter to make sure the while loop does not get stuck in deadlock
	 * @param fileSize - the size of the file
	 * @param dataForm - the date format to be used when outputting the last modified date
	 * readDataBlock will take a block number, read information about every file & directory in the block
	 * and output it to the screen
	 */
	public void readDataBlock(int blockNum)
	{
		int fileNameLen;
		int length = 0;
		int blockPos = 0;
		int traverseBlock = 0;
		int fileSize = 0;
		SimpleDateFormat dateForm = new SimpleDateFormat("MMM-dd hh:mm");
		
		System.out.println("");
		System.out.println(">> Jumping to data block " + blockNum + "...");
		System.out.println("");
		System.out.format(" > | %-10s | %-10s | %-6s | %-7s | %-9s | %-13s | %-11s |\n","File Perms","Hard Links","UserID","GroupID","File Size","Last Modified","File Name");
		System.out.println(" > |======================================================================================|");
		
		while(blockPos < 1024 && traverseBlock < 1024)
		{
			fileNameLen = (int) ext.readBlock(blockNum,blockPos + 6,1).get();
			length = (int) ext.readBlock(blockNum,blockPos + 4,2).getShort();
			fileSize = getSizeOfFile(ext.readBlock(blockNum,blockPos,4).getInt()).getInt();
			
			if(ext.readBlock(blockNum,blockPos,4).getInt() != 0 || ext.readBlock(blockNum,blockPos + 4,2).getShort() != 0)
			{
				System.out.format(" > | %-10s | %-10s | %-6s | %-7s | %-9s | %-13s | %-11s |\n",
						convertFileMode(readInode(ext.readBlock(blockNum,blockPos,4).getInt(),0,2).getShort()),
						readInode(ext.readBlock(blockNum,blockPos,4).getInt(),26,2).getShort(),
						readInode(ext.readBlock(blockNum,blockPos,4).getInt(),2,2).getShort(),
						readInode(ext.readBlock(blockNum,blockPos,4).getInt(),24,2).getShort(),
						fileSize,
						dateForm.format(readInode(ext.readBlock(blockNum,blockPos,4).getInt(),16,4).getInt() * 1000L),
						new String(ext.readBlock(blockNum,blockPos + 8,fileNameLen).array()));
			}
			else
			{
				System.out.format(" > | %-84s |\n","No data found! ");
			}
			blockPos += length;
			traverseBlock++;
		}
	}
	
	/**
	 * 
	 * @param inode - the inode to read
	 * @param dataExists - a flag to determine if the data blocks contained any data at all
	 * readDataBlocks will take an inode and then cycle through each data block pointer.
	 * If the pointer is more than 0 (there is something inside it) It will try to read the
	 * inodes inside the data block.
	 */
	public void readDataBlocks(int inode)
	{
		boolean dataExist = false;
		
		for(int i = 40; i < 84; i += 4)
		{
			dataBlockPoint = readInode(inode,i,4).getInt();
			if(dataBlockPoint > 0)
			{
				readDataBlock(dataBlockPoint);	
				dataExist = true;
			}
		}
				
		if(!dataExist)
		{
			System.out.println(">> No data pointers found at inode " + inode);
		}
	}
	
	/**
	 * readDirectInfo reads the current directory that the user is currently
	 * in and display more information about the files such as the inode numbers
	 * for each file.
	 */
	public void readDirectInfo()
	{
		for(int i = 40; i < 84; i += 4)
		{
			dataBlockPoint = readInode(currentDirect,i,4).getInt();
			if(dataBlockPoint > 0)
			{
				directInfo(dataBlockPoint);	
			}
		}
	}
	
	/**
	 * 
	 * @param blockNum - the block number to read
	 * @param fileNameLen - the length of the file name
	 * @param length - the length of the inode
	 * @param blockPos - the offset to read from
	 * @param traverseBlock - A counter to make sure the while loop does not get stuck in deadlock
	 * directInfo takes a block number and outputs extra information based on the directory the user is currently in
	 */
	public void directInfo(int blockNum)
	{
		int fileNameLen;
		int length = 0;
		int blockPos = 0;
		int traverseBlock = 0;
		
		System.out.format("\n > | %-5s | %-6s | %-3s | %-12s |\n","Inode","Length","FNL","File Name");
		System.out.println(" > |=====================================|");
		
		while(blockPos < 1024 && traverseBlock < 1024)
		{
			fileNameLen = (int) ext.readBlock(blockNum,blockPos + 6,1).get();
			length = (int) ext.readBlock(blockNum,blockPos + 4,2).getShort();
			
			System.out.format(" > | %-5s | %-6s | %-3s | %-12s |\n",
					ext.readBlock(blockNum,blockPos,4).getInt(),
					ext.readBlock(blockNum,blockPos + 4,2).getShort(),
					ext.readBlock(blockNum,blockPos + 6,1).get(),
					new String(ext.readBlock(blockNum,blockPos + 8,fileNameLen).array()));
			
			blockPos += length;
			traverseBlock++;
		}
	}
	
	/**
	 * 
	 * @param fileMode - the file mode read from the inode
	 * @return fileModeRWX - the new string created from filemode
	 * convertFileMode will take the fileMode of an inode and decipher it so you
	 * can tell if it's a directory and it's permissions for each group
	 */
	public String convertFileMode(short fileMode)
    {
        String fileModeRWX = "";
        
        // Is it a directory
        if((fileMode & 0x4000) != 0)
        {
        	fileModeRWX += "d";
        }
        else
        {
        	fileModeRWX += "-";
        }
        
        // Perms for the user
        if((fileMode & 0x0100) != 0)
        {
        	fileModeRWX += "r";
        }
        else
        {
        	fileModeRWX += "-";
        }
        
        if((fileMode & 0x0080) != 0)
        {
        	fileModeRWX += "w";
        }
        else
        {
        	fileModeRWX += "-";
        }
        
        if((fileMode & 0x0040) != 0)
        {
        	fileModeRWX += "x";
        }
        else
        {
        	fileModeRWX += "-";
        }
        
        // Perms for the group
        if((fileMode & 0x0020) != 0)
        {
        	fileModeRWX += "r";
        }
        else
        {
        	fileModeRWX += "-";
        }
        
        if((fileMode & 0x0010) != 0)
        {
        	fileModeRWX += "w";
        }
        else
        {
        	fileModeRWX += "-";
        }
        
        if((fileMode & 0x0008) != 0)
        {
        	fileModeRWX += "x";
        }
        else
        {
        	fileModeRWX += "-";
        }
        
        // Perms for others
        if((fileMode & 0x0004) != 0)
        {
        	fileModeRWX += "r";
        }
        else
        {
        	fileModeRWX += "-";
        }
        
        if((fileMode & 0x0002) != 0)
        {
        	fileModeRWX += "w";
        }
        else
        {
        	fileModeRWX += "-";
        }
        
        if((fileMode & 0x0001) != 0)
        {
        	fileModeRWX += "x";
        }
        else
        {
        	fileModeRWX += "-";
        }
        
        return fileModeRWX;
    }
	
	/**
	 * 
	 * @param pointer - the indirect/double indirect/triple indirect block pointer
	 * @param indirectNum the number of indirections
	 * 
	 * @param blockToSearch[] - an array holding the block number to search
	 * @param blockToReadFrom - the block to read text from
	 * @param fileString - the output of what is stored in the indirect/double indirect/triple indirect block pointer
	 * readIndirectDataBlock will take the indirect pointer and the number of indirections to cycle through each block
	 * depending on the levels of indirection. If it finds a datablock that is not empty, it will enter it and cycle
	 * through its data blocks until it arrives at the last point of indirection and read any data blocks it finds as text
	 */
	public void readIndirectDataBlock(int pointer, int indirectNum)
	{
		int blockToSearch[] = new int[indirectNum];
		int blockToReadFrom = 0; 
		String fileString = "";
		
		blockToSearch[0] = pointer;
		
		switch(indirectNum)
		{
			case 1:
			{
				for(int i = 0; i < 256; i ++)
				{			
					if(ext.readBlock(blockToSearch[0],i * 4,4).getInt() != 0)
					{
						blockToReadFrom = ext.readBlock(blockToSearch[0],i * 4,4).getInt();
						try
						{
							fileString = new String(ext.readBlock(blockToReadFrom,0,1024).array(), "UTF-8");
						} catch (UnsupportedEncodingException e)
						{
							e.printStackTrace();
						}
					}
				}
				System.out.println(fileString.trim());
				break;
			}
			case 2:
			{
				for(int i = 0; i < 256; i ++)
				{			
					if(ext.readBlock(blockToSearch[0],i * 4,4).getInt() != 0)
					{
						blockToSearch[1] = ext.readBlock(blockToSearch[0],i * 4,4).getInt();
						
						for(int j = 0; j < 256; j ++)
						{
							if(ext.readBlock(blockToSearch[1],j * 4,4).getInt() != 0)
							{
								blockToReadFrom = ext.readBlock(blockToSearch[1],j * 4,4).getInt();
								try
								{
									fileString =  new String(ext.readBlock(blockToReadFrom,0,1024).array(), "UTF-8");
								} catch (UnsupportedEncodingException e)
								{
									e.printStackTrace();
								}
							}
						}
					}
				}
				System.out.println(fileString.trim());
				break;
			}
			case 3:
			{
				for(int i = 0; i < 256; i ++)
				{			
					if(ext.readBlock(blockToSearch[0],i * 4,4).getInt() != 0)
					{
						blockToSearch[1] = ext.readBlock(blockToSearch[0],i * 4,4).getInt();
						
						for(int j = 0; j < 256; j ++)
						{
							if(ext.readBlock(blockToSearch[1],j * 4,4).getInt() != 0)
							{
								blockToSearch[2] = ext.readBlock(blockToSearch[1],j * 4,4).getInt();
								
								for(int k = 0; k < 256; k ++)
								{
									if(ext.readBlock(blockToSearch[2],k * 4,4).getInt() != 0)
									{
										blockToReadFrom = ext.readBlock(blockToSearch[2],k * 4,4).getInt();
										try
										{
											fileString =  new String(ext.readBlock(blockToReadFrom,0,1024).array(), "UTF-8");
										} catch (UnsupportedEncodingException e)
										{
											e.printStackTrace();
										}
									}
								}
							}
						}
					}
				}
				System.out.println(fileString.trim());
				break;
			}
		}
	}
	
	/**
	 * 
	 * @param blockNum - the block number to read
	 * readIndirectDataBlockManual like readIndirectDataBlock, except the difference being it allows you to
	 * manually look around in each level of indirection, instead of automatically cycling through.
	 */
	public void readIndirectDataBlockManual(int blockNum)
	{		
		for(int i = 0; i < 256; i ++)
		{
			dataBlockPoint = blockNum;
			
			if(Driver.getIsEmpty())
			{
				System.out.println(" > Data block pointer " + (i + 1) + "/256: " + ext.readBlock(blockNum,i * 4,4).getInt());
			}
			else
			{
				if(ext.readBlock(blockNum,i * 4,4).getInt() != 0)
				{
					System.out.println(" > Data block pointer " + (i + 1) + "/256: " + ext.readBlock(blockNum,i * 4,4).getInt());
				}
			}
		}
	}
	
	/**
	 * 
	 * @param inode - the inode to get the filesize of
	 * @return byteBuffer - the buffer with the complete filesize
	 * getSizeOfFile will take an inode and output the filesize of that inode. Due to the way the filesize is stored
	 * (in 2 pieces) this method will grab both sets of bytes and put them together to output the correct filesize.
	 */
	public ByteBuffer getSizeOfFile(int inode)
	{
		byte[] fileSize = new byte[8];
		
		int inoGroup = (inode - 1) / inodesPerGroup;
		int inoIndex = (inode - 1) % inodesPerGroup;
		
		if(inoGroup <= blockGroups)
		{
			int inoTabPos = inodeTablePoint[inoGroup] * 1024; //block size
			int inoPos = inoTabPos + (inoIndex * sizeOfInodes);
			
			try
			{
				ext.file.seek(inoPos + 4);
				ext.file.read(fileSize,0,4);
				ext.file.seek(inoPos + 108);
				ext.file.read(fileSize,4,4);
			} catch (IOException e)
			{
				e.printStackTrace();
			}
			
			ByteBuffer size = ByteBuffer.wrap(fileSize);
			size.order(ByteOrder.LITTLE_ENDIAN);
			return size;
		}
		else
		{
			System.out.println(">> This file's inode is invalid");
			return ext.readFile(0,0);
		}	
	}
	
	/**
	 * readCurrentDiret will display the list of files and folders if the directory the
	 * user is currently in
	 */
	public void readCurrentDirect()
	{
		readDataBlocks(currentDirect);
	}
	
	/**
	 * 
	 * @return getSizeOfFile - the size of the current directory's file
	 * getSizeOfDirectFile will return the file size of the current directory
	 */
	public int getSizeOfDirectFile()
	{
		return getSizeOfFile(currentDirect).getInt();
	}
	
	/**
	 * 
	 * @param fileName - the name of the file or folder to navigate to
	 * @param numOfFileNames - the number of filename destinations to be made
	 * @param indirect - forces the method to read from an indirect pointer instead of a direct one (0 for direct, 1 for indirect)
	 * 
	 * @param fileNameLen - the length of the file name
	 * @param length - the length of the inode
	 * @param blockPos - the offset to read from
	 * @param traverseBlock - A counter to make sure the while loop does not get stuck in deadlock
	 * @param directFound - a flag to tell if the directory being searched for was found
	 * readFileName will take a filename (which can be a single filename or multiple filenames),
	 * the number of file names it must find destinations to, and if it should be looking at direct or indirect pointers.
	 * It will use the currently directory as a starting place to search from, unless the filename starts with root. In which case
	 * the search will start at root
	 */
	public void readFileName(String fileName, int numOfFileNames, int indirect)
	{
		int fileNameLen; 
		int length = 0; // length of the entire inode to read
		int blockPos = 0;
		int traverseBlock = 0;
		boolean directFound = false;
		
		for(int i = 0; i < 12; i ++)
		{
			dataBlockPoint = readInode(currentDirect,(i * 4) + 40,4).getInt();
			if(dataBlockPoint > 0)
			{
				while(blockPos < 1024 && traverseBlock < 1024)
				{
					fileNameLen = (int) ext.readBlock(dataBlockPoint,blockPos + 6,1).get();
					length = (int) ext.readBlock(dataBlockPoint,blockPos + 4,2).getShort();
					String fileNameSearch = new String(ext.readBlock(dataBlockPoint,blockPos + 8,fileNameLen).array());
					
					if(ext.readBlock(dataBlockPoint,blockPos,4).getInt() != 0 || ext.readBlock(dataBlockPoint,blockPos + 4,2).getShort() != 0)
					{
						if(fileName.equals(fileNameSearch))
						{
							countDirect++;

							if(indirect > 0)
							{
								if(readInode(ext.readBlock(dataBlockPoint,blockPos,4).getInt(),88,4).getInt() != 0)
								{
									System.out.println("Indirect");
									readIndirectDataBlock(readInode(ext.readBlock(dataBlockPoint,blockPos,4).getInt(),88,4).getInt(),1);
								}
								if(readInode(ext.readBlock(dataBlockPoint,blockPos,4).getInt(),92,4).getInt() != 0)
								{
									System.out.println("dbl Indirect");
									readIndirectDataBlock(readInode(ext.readBlock(dataBlockPoint,blockPos,4).getInt(),92,4).getInt(),2);
								}
								if(readInode(ext.readBlock(dataBlockPoint,blockPos,4).getInt(),96,4).getInt() != 0)
								{
									System.out.println("trpl Indirect");
									readIndirectDataBlock(readInode(ext.readBlock(dataBlockPoint,blockPos,4).getInt(),96,4).getInt(),3);
								}
							}
							else
							{
								if(ext.readBlock(dataBlockPoint,blockPos + 7,1).get() == 1) // This file is a regular file
								{
									readTxt(ext.readBlock(dataBlockPoint,blockPos,4).getInt(),0);
								}
								else if(ext.readBlock(dataBlockPoint,blockPos + 7,1).get() == 2) // This file is a directory
								{
									nextDirect = ext.readBlock(dataBlockPoint,blockPos,4).getInt();
									currentDirect = nextDirect;	
								}
								else
								{
									System.out.println("File type not recognised");
								}
							}
							
							directFound = true;
							
							if(countDirect >= numOfFileNames)
							{
								readDataBlocks(nextDirect);
								countDirect = 0;
							}
							break;
						}
					}
					blockPos += length;
					traverseBlock++;
				}
			}
		}
		if(fileName.equals("root"))
		{
			countDirect++;
			nextDirect = 2;
			currentDirect = nextDirect;
			directFound = true;
			
			if(countDirect >= numOfFileNames)
			{
				readDataBlocks(nextDirect);
				countDirect = 0;
			}
		}
		
		if(!directFound)
		{
			System.out.println(" >> No such file exists in this directory");
		}
	}
	
	/**
	 * 
	 * @param dataNum - the inode/block number/byte number to be read
	 * @param type - the type of data. 0 being an inode, 1 being a block, 2 being a byte
	 * @param length - the number of bytes to read
	 * getHexByte will take an inode/block/byte number, a length to read and call a method to
	 * read bytes and print them as hex
	 */
	public void getHexByte(int dataNum,int type, int length)
	{
		System.out.format(">> Reading data as Hex...\n\n");
		switch(type)
		{
			case 0: // inode datatype
			{
				for(int i = 40; i < 84; i += 4)
				{
					dataBlockPoint = readInode(dataNum,i,4).getInt();
					if(dataBlockPoint > 0)
					{
						hlp.readHex(ext.readBlock(dataBlockPoint,0,length));
					}
				}				
				break;
			}
			case 1: // block number datatype
			{
				hlp.readHex(ext.readBlock(dataNum,0,length));
				break;
			}
			case 2: // bytes datatype
			{
				hlp.readHex(ext.readFile(dataNum,length));
				break;
			}	
		}
	}
	
	/**
	 * terminate will stop the current session and close the file
	 */
	public void terminate()
	{
		try
		{
			ext.file.close();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		System.exit(0);
	}
	
	/**
	 * @return totalBytes - the total size of the ext2 filesystem in bytes
	 */
	public long getTotalFileSize()
	{
		return totalBytes;
	}
}

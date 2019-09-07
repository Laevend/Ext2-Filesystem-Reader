import java.io.IOException;
import java.util.Scanner;

public class Driver
{
	/**
	 * @param viewEmpty - Holds a boolean value which decides to display empty data block pointers during the current session
	 * @param sc - A scanner used to take user input
	 */
	private static boolean viewEmpty = false;
	static Scanner sc = new Scanner(System.in);
	
	/**
	 * This method outputs a question that appears at the start of every session and will continue
	 * to run until the user answers with either a y (yes) or n (no).
	 * The question is asking the user if they want the API to display empty data block pointers in
	 * this session. The reason being that displaying empty data block pointers is not useful
	 */
	public static void emptyDataPointers()
	{
		String viewEmptyAnswer;
		
		System.out.println("Would you like to display empty data block pointers?[Y/N]");
		System.out.println("");
		System.out.print(" > ");
		viewEmptyAnswer = sc.nextLine();
		
		if(viewEmptyAnswer.equals("Y") || viewEmptyAnswer.equals("y"))
		{
			viewEmpty = true;
		}
		else if(viewEmptyAnswer.equals("N") || viewEmptyAnswer.equals("n"))
		{
			viewEmpty = false;
		}
		else
		{
			System.out.format("Not a valid answer!\n\n");
			emptyDataPointers();
		}
	}
	
	/**
	 * This is the main method and it is also the shell or command line interface which
	 * the user interacts with.
	 * The user can enter a command that currently exists and execute it using some parameters.
	 * The user will get an error along with some information on what commands can be used if they
	 * enter a command that does not exist or enter incorrect syntax for a command.
	 * @param vol - The ext2 filesystem
	 * @param tempCmd - Holds the users initial inputed command
	 * @param cmd - Holds the users inputed command with extra spaces taken out
	 * @param cmdParts[] - Splits the command up into an array of strings
	 */
	public static void main(String args[]) throws IOException
	{
		divider(); 
		System.out.println("Volume Structure for ext2 File System");
		divider();
		
		Volume vol = new Volume("ext2fs");
		
		String tempCmd;
		String cmd;
		String[] cmdParts;
		
		divider();
		// ask user if they want to display empty data block pointers
		emptyDataPointers();		
		divider();
		
		while(true)
		{
			System.out.print(" > ");
			tempCmd = sc.nextLine();
			cmd = tempCmd.trim().replaceAll(" +", " "); // Remove multiple whitespace
			cmd.toLowerCase();
			cmdParts = cmd.split(" ");
			
			/**
			 *  <> ======================================================= <>
			 * <<< HELP COMMAND >>>
			 * This command is used to display command syntax.
			 * It tells the user how to enter commands.
			 */
			if(cmdParts[0].equals("help") || cmdParts[0].equals("h"))
			{
				System.out.format(" >>>> Command Syntax <<<<\n\n");
				System.out.println(" >>> Inode <<<"); // Reads an inode directly from the filesystem
				System.out.format(" > ino [inode number] [offset] [length]\n\n");
				System.out.println(" >>> Block <<<"); // Reads a block directly from the filesystem
				System.out.format(" > blk [block number] [offset] [length]\n\n");
				System.out.println(" >>> Byte <<<"); // Reads a byte directly from the filesystem
				System.out.format(" > byt [byte number] [length]\n\n");
				System.out.println(" >>> Display <<<"); // Display is used to display information about indodes and their datablocks
				System.out.println(" > display <ino/blk/hex/txt/blki>");
				System.out.println(" > display ino [inode number]"); // Display the contents of an inode
				System.out.println(" > display blk [block number]"); // Display the contents of a block
				System.out.println(" > display hex <ino/blk/byt>");
				System.out.println("	.. hex ino [inode number] (length)"); // Display the hex dump of an inode
				System.out.println("	.. hex blk [block number] (length)"); // Display the hex dump of a block
				System.out.println("	.. hex byt [byte number] (length)"); // Display the hex dump of bytes
				System.out.println(" > display txt <ino/blk>");
				System.out.println("	.. txt ino [inode number]"); // Display text from an inode
				System.out.println("	.. txt blk [block number]"); // Display text from a block number
				System.out.format(" > display blki [indirect block number]\n\n"); // Display the contents of an indirect block
				System.out.println(" >>> Root Directory <<<");
				System.out.format(" > root\n\n"); // Display the root
				System.out.println(" >>> Navigation <<<");
				System.out.format(" > cd <[file path] / [file name]>\n\n"); // Navigate through the filesystem
				System.out.println(" >>> Display Directory <<<");
				System.out.format(" > dir\n\n"); // Display the current directory
				System.out.println(" >>> Display Superblock <<<");
				System.out.format(" > super\n\n"); // Display the superblock
				System.out.println(" >>> Display More Info <<<");
				System.out.format(" > info\n\n"); // Display more information about the directory
				System.out.println(" >>> File Pointer Position <<<");
				System.out.format(" > seek\n\n"); // Display the current position of the file pointer
				System.out.println(" >>> Read File From Pointer Position <<<");
				System.out.format(" > read [length]\n\n"); // Display the information read from the current position of the file pointer
				System.out.println(" >>> Current Directorys File Size <<<");
				System.out.format(" > size\n\n"); // Display the file size of the current directory
				System.out.println(" >>> Exit <<<");
				System.out.format(" > exit\n\n"); // Exit the shell
				System.out.format(" >>> Key <<<\n\n");
				System.out.format(" > %-15s %-75s\n","[...]"," - Mandatory user input: You must enter a value in this space");
				System.out.format(" > %-15s %-75s\n","<.../...>"," - Choice command: You must use one command to use from here");
				System.out.format(" > %-15s %-75s\n","<[...] / [...]>"," - Choice Mandatory user input: You must use one of the amount provided");
				System.out.format(" > %-15s %-75s\n","(...)"," - Optional user input: You can optionally add an extra value here");
				divider();
			}
			/**
			 *  <> ======================================================= <>
			 * <<< INO/BLK/BYT COMMAND >>>
			 * This command is used to display an arbitrary inode, block, or byte number within the filesystem.
			 * @param isNumeric[] - Holds 3 boolean values which are used to indicate if the expected values entered are numerical
			 * @param dataNum - The inode number, block number, or byte number to read from
			 * @param offset - The number of bytes to offset from
			 * @param length - The number of bytes to read
			 */
			else if(cmdParts[0].equals("ino") || cmdParts[0].equals("blk") || cmdParts[0].equals("byt"))
			{
				boolean[] isNumeric = new boolean[3];
				int dataNum;
				int offset;
				int length;
				
				if(cmd.length() <= 4) // check that not only the command keyword has been entered
				{
					syntaxError();
				}
				else
				{					
					// These 2 commands accept 3 parameters
					if(cmdParts[0].equals("ino") || cmdParts[0].equals("blk"))
					{
						isNumeric[0] = cmdParts[1].trim().matches("^[0-9]+$");
						isNumeric[1] = cmdParts[2].trim().matches("^[0-9]+$");
						isNumeric[2] = cmdParts[3].trim().matches("^[0-9]+$");
						
						if(isNumeric[0] && isNumeric[1] && isNumeric [2])
						{
							dataNum = Integer.parseInt(cmdParts[1].trim());
							offset = Integer.parseInt(cmdParts[2].trim());
							length = Integer.parseInt(cmdParts[3].trim());
							
							if(dataNum < vol.getTotalFileSize() || offset < vol.getTotalFileSize() || length < vol.getTotalFileSize())
							{
								if(cmdParts[0].equals("ino"))
								{
									System.out.println("Printing " + length + " bytes from inode " + dataNum);
									
									if(length >= 4)
									{
										System.out.println(" > " + vol.readInode(dataNum, offset, length).getInt());
									}
									else if(length >= 2)
									{
										System.out.println(" > " + vol.readInode(dataNum, offset, length).getShort());
									}
									else if(length == 1)
									{
										System.out.println(" > " + vol.readInode(dataNum, offset, length).get());
									}
								}
								else if(cmdParts[0].equals("blk"))
								{
									System.out.println("Printing " + length + " bytes from block " + dataNum);
									
									if(length >= 4)
									{
										System.out.println(" > " + vol.ext.readBlock(dataNum, offset, length).getInt());
									}
									else if(length >= 2)
									{
										System.out.println(" > " + vol.ext.readBlock(dataNum, offset, length).getShort());
									}
									else if(length == 1)
									{
										System.out.println(" > " + vol.ext.readBlock(dataNum, offset, length).get());
									}
									else
									{
										syntaxError();
									}
								}
							}
						}
						else
						{
							syntaxError();
						}
					}
					// This command only accepts 2 parameters
					else if(cmdParts[0].equals("byt"))
					{
						isNumeric[0] = cmdParts[1].trim().matches("^[0-9]+$");
						isNumeric[1] = cmdParts[2].trim().matches("^[0-9]+$");
						
						if(isNumeric[0] && isNumeric[1])
						{
							dataNum = Integer.parseInt(cmdParts[1].trim());
							length = Integer.parseInt(cmdParts[2].trim());
							
							if(dataNum < vol.getTotalFileSize() || length < vol.getTotalFileSize())
							{
								System.out.println("Printing " + length + " bytes from byte " + dataNum);
								
								if(length >= 4)
								{
									System.out.println(" > " + vol.ext.readFile(dataNum, length).getInt());
								}
								else if(length >= 2)
								{
									System.out.println(" > " + vol.ext.readFile(dataNum, length).getShort());
								}
								else if(length == 1)
								{
									System.out.println(" > " + vol.ext.readFile(dataNum, length).get());
								}
								else
								{
									syntaxError();
								}
							}
						}
						else
						{
							syntaxError();
						}
					}
					else
					{
						syntaxError();
					}
				}
				divider();
			}
			/**
			 *  <> ======================================================= <>
			 * <<< DISPLAY COMMAND >>>
			 * This command is used to "display" information about the filesystem in ways entered by the user.
			 * @param isNumeric - Holds a boolean value which is used to indicate if the expected value entered is numerical
			 */
			else if(cmdParts[0].equals("display"))
			{
				boolean isNumeric = false;
				
				if(cmd.length() <= 8) // check that not only the command keyword has been entered
				{
					syntaxError();
				}
				/**
				 *  <> ======================================================= <>
				 * <<< DISPLAY INO COMMAND >>>
				 * This command is a sub-command of "display" and is used to display an inode in it's entirety.
				 */
				else if(cmdParts[1].trim().equals("ino"))
				{
					if(cmd.length() >= 9 && cmd.length() <= 12) // check that not only the command keyword has been entered
					{
						syntaxError();
					}
					else
					{
						isNumeric = cmdParts[2].trim().matches("^[0-9]+$");
					}
					
					if(isNumeric)
					{
						int inodeNum = Integer.parseInt(cmdParts[2].trim());
						
						if(inodeNum < vol.getTotalFileSize())
						{
							vol.displayIno(inodeNum);
							divider();
						}
						else
						{
							syntaxError();
						}
					}
				}
				/**
				 *  <> ======================================================= <>
				 * <<< DISPLAY TXT COMMAND >>>
				 * This command is a sub-command of "display" and is used to display text files from data block pointers
				 */
				else if(cmdParts[1].trim().equals("txt"))
				{		
					if(cmd.length() >= 9 && cmd.length() <= 12) // check that not only the command keyword has been entered
					{
						syntaxError();
					}
					/**
					 *  <> ======================================================= <>
					 * <<< DISPLAY TXT INO COMMAND >>>
					 * This command is a sub-command of "display txt" and is used to display the text from all the
					 * data block pointers inside a given inode.
					 */
					else if(cmdParts[2].trim().equals("ino"))
					{
						if(cmd.length() >= 13 && cmd.length() <= 16) // check that not only the command keyword has been entered
						{
							syntaxError();
						}
						else
						{
							isNumeric = cmdParts[3].trim().matches("^[0-9]+$");
						}
						
						if(isNumeric)
						{
							int inodeNum = Integer.parseInt(cmdParts[3].trim());
							
							if(inodeNum < vol.getTotalFileSize())
							{
								vol.readTxt(inodeNum,0);
								divider();
							}
							else
							{
								syntaxError();
							}
						}
					}
					/**
					 *  <> ======================================================= <>
					 * <<< DISPLAY TXT BLK COMMAND >>>
					 * This command is a sub-command of "display txt" and is used to display the text inside of a single
					 * data block pointer.
					 */
					else if(cmdParts[2].trim().equals("blk"))
					{
						if(cmd.length() >= 13 && cmd.length() <= 16) // check that not only the command keyword has been entered
						{
							syntaxError();
						}
						else
						{
							isNumeric = cmdParts[3].trim().matches("^[0-9]+$");
						}
						
						if(isNumeric)
						{
							int blockNum = Integer.parseInt(cmdParts[3].trim());
							
							if(blockNum < vol.getTotalFileSize())
							{
								vol.readTxt(blockNum,1);
								divider();
							}
							else
							{
								syntaxError();
							}
						}
					}
				}
				/**
				 *  <> ======================================================= <>
				 * <<< DISPLAY HEX COMMAND >>>
				 * This command is a sub-command of "display" and is used to dump bytes printed as hexadecimal in the console
				 * along with the characters counterpart.
				 */
				else if(cmdParts[1].trim().equals("hex"))
				{					
					if(cmd.length() >= 9 && cmd.length() <= 12) // check that not only the command keyword has been entered
					{
						syntaxError();
					}
					/**
					 *  <> ======================================================= <>
					 * <<< DISPLAY HEX INO COMMAND >>>
					 * This command is a sub-command of "display hex" and is used to dump bytes as hex from a given inode
					 */
					else if(cmdParts[2].trim().equals("ino"))
					{
						if(cmd.length() >= 13 && cmd.length() <= 16) // check that not only the command keyword has been entered
						{
							syntaxError();
						}
						else
						{
							isNumeric = cmdParts[3].trim().matches("^[0-9]+$");
						}
						
						if(isNumeric)
						{
							int hexNum = Integer.parseInt(cmdParts[3].trim());
							
							if(hexNum < vol.getTotalFileSize())
							{
								if(cmd.length() > (cmdParts[3].trim().length() + 17))
								{
									int hexBytesLen = Integer.parseInt(cmdParts[4].trim());
									
									if(hexBytesLen < vol.getTotalFileSize())
									{
										vol.getHexByte(hexNum,0,hexBytesLen);
									}
									else
									{
										syntaxError();
									}
								}
								else
								{
									vol.getHexByte(hexNum,0,1024);
								}
							}
							else
							{
								syntaxError();
							}
						}
						divider();
					}
					/**
					 *  <> ======================================================= <>
					 * <<< DISPLAY HEX BLK COMMAND >>>
					 * This command is a sub-command of "display hex" and is used to dump bytes as hex from a given block
					 */
					else if(cmdParts[2].trim().equals("blk"))
					{
						if(cmd.length() >= 13 && cmd.length() <= 16) // check that not only the command keyword has been entered
						{
							syntaxError();
						}
						else
						{
							isNumeric = cmdParts[3].trim().matches("^[0-9]+$");
						}
						
						if(isNumeric)
						{
							int hexNum = Integer.parseInt(cmdParts[3].trim());
							
							if(hexNum < vol.getTotalFileSize())
							{
								if(cmd.length() > (cmdParts[3].trim().length() + 17))
								{
									int hexBytesLen = Integer.parseInt(cmdParts[4].trim());
									
									if(hexBytesLen < vol.getTotalFileSize())
									{
										vol.getHexByte(hexNum,1,hexBytesLen);
									}
									else
									{
										syntaxError();
									}
								}
								else
								{
									vol.getHexByte(hexNum,1,1024);
								}
							}
							divider();
						}
					}
					/**
					 *  <> ======================================================= <>
					 * <<< DISPLAY HEX BYT COMMAND >>>
					 * This command is a sub-command of "display hex" and is used to dump bytes as hex from a given byte
					 */
					else if(cmdParts[2].trim().equals("byt"))
					{
						if(cmd.length() >= 13 && cmd.length() <= 16) // check that not only the command keyword has been entered
						{
							syntaxError();
						}
						else
						{
							isNumeric = cmdParts[3].trim().matches("^[0-9]+$");
						}
						
						if(isNumeric)
						{
							int hexNum = Integer.parseInt(cmdParts[3].trim());
							
							if(hexNum < vol.getTotalFileSize())
							{
								if(cmd.length() > (cmdParts[3].trim().length() + 17))
								{
									int hexBytesLen = Integer.parseInt(cmdParts[4].trim());
									
									if(hexBytesLen < vol.getTotalFileSize())
									{
										vol.getHexByte(hexNum,2,hexBytesLen);
									}
									else
									{
										syntaxError();
									}	
								}
								else
								{
									vol.getHexByte(hexNum,2,1024);
								}
							}
							divider();
						}
					}
					else
					{
						syntaxError();
					}
				}
				/**
				 *  <> ======================================================= <>
				 * <<< DISPLAY BLK COMMAND >>>
				 * This command is a sub-command of "display" and is used to display the contents of a block
				 */
				else if(cmdParts[1].trim().equals("blk"))
				{					
					if(cmd.length() >= 9 && cmd.length() <= 12) // check that not only the command keyword has been entered
					{
						syntaxError();
					}
					else
					{
						isNumeric = cmdParts[2].trim().matches("^[0-9]+$");
					}
					
					if(isNumeric)
					{
						int blkNum = Integer.parseInt(cmdParts[2].trim());
						
						if(blkNum < vol.getTotalFileSize())
						{
							vol.readDataBlock(blkNum);
							divider();
						}
					}
				}
				/**
				 *  <> ======================================================= <>
				 * <<< DISPLAY BLKI COMMAND >>>
				 * This command is a sub-command of "display" and is used to display the contents of a file given an 
				 * indirect block pointer
				 */
				else if(cmdParts[1].trim().equals("blki"))
				{
					
					if(cmd.length() >= 9 && cmd.length() <= 13) // check that not only the command keyword has been entered
					{
						syntaxError();
					}
					else
					{
						isNumeric = cmdParts[2].trim().matches("^[0-9]+$");
					}
					
					if(isNumeric)
					{
						int blkNum = Integer.parseInt(cmdParts[2].trim());
						
						if(blkNum < vol.getTotalFileSize())
						{
							vol.readIndirectDataBlockManual(blkNum);
							divider();
						}
					}
				}
				else
				{
					syntaxError();
				}
			}
			/**
			 *  <> ======================================================= <>
			 * <<< SUPER COMMAND >>>
			 * This command is used to print out the super block and group descriptors inode table pointers
			 */
			else if(cmdParts[0].equals("super"))
			{
				vol.readSuperBlock();
				divider();
			}
			/**
			 *  <> ======================================================= <>
			 * <<< EXIT COMMAND >>>
			 * This command terminates the current session
			 */
			else if(cmdParts[0].equals("exit"))
			{
				vol.terminate();
			}
			/**
			 *  <> ======================================================= <>
			 * <<< ROOT COMMAND >>>
			 * This command displays information about the root of the filesystem, including the inode and
			 * the data block pointers
			 */
			else if(cmdParts[0].equals("root"))
			{
				vol.displayIno(2);
				vol.readDataBlocks(2);
				divider();	
			}
			/**
			 *  <> ======================================================= <>
			 * <<< CD COMMAND >>>
			 * This command is used to traverse the file system 
			 */
			else if(cmdParts[0].equals("cd"))
			{				
				if(cmd.length() <= 3) // display command with and without a space
				{
					syntaxError();
				}
				else
				{
					String[] cdParts = cmd.split(" ");
					String path = cdParts[1].trim().replaceAll("/"," ");
					String[] fileName = path.split(" ");
					
					for(int i = 0; i < fileName.length; i++)
					{
						if(cdParts.length > 2)
						{
							vol.readFileName(fileName[i],fileName.length,1);
						}
						else
						{
							vol.readFileName(fileName[i],fileName.length,0);
						}	
					}
				}	
				divider();	
			}
			/**
			 *  <> ======================================================= <>
			 * <<< DIR COMMAND >>>
			 * This command displays the files in the directory you are in
			 */
			else if(cmdParts[0].equals("dir"))
			{
				vol.readCurrentDirect();
				divider();
			}
			/**
			 *  <> ======================================================= <>
			 * <<< INFO COMMAND >>>
			 * This command displays more information about the directory you are in
			 * such as the inodes, length of each inode etc.
			 */
			else if(cmdParts[0].equals("info"))
			{
				vol.readDirectInfo();
				divider();
			}
			/**
			 *  <> ======================================================= <>
			 * <<< SEEK COMMAND >>>
			 * This command displays the current position of the pointer (in bytes) in the filesystem
			 */
			else if(cmdParts[0].equals("seek"))
			{
				System.out.println(" > Current position in filesystem is at byte: " + vol.ext.getPosition());
				divider();
			}
			/**
			 *  <> ======================================================= <>
			 * <<< READ COMMAND >>>
			 * This command reads a number of bytes from the current file pointer
			 */
			else if(cmdParts[0].equals("read"))
			{
				boolean isNumeric = false;
				
				if(cmd.length() <= 5) // check that not only the command keyword has been entered
				{
					syntaxError();
				}
				else
				{
					isNumeric = cmdParts[1].trim().matches("^[0-9]+$");
				}
				
				if(isNumeric)
				{
					int redNum = Integer.parseInt(cmdParts[1].trim());
					
					if(redNum < vol.getTotalFileSize())
					{
						if(redNum >= 4)
						{
							System.out.println("Got Int " + vol.ext.readSeek(redNum).getInt());
						}
						else if(redNum >= 2 && redNum <= 3)
						{
							System.out.println("Got Short " + vol.ext.readSeek(redNum).getShort());
						}
						else if(redNum == 1)
						{
							System.out.println("Got byte " + vol.ext.readSeek(redNum).get());
						}
						else
						{
							System.out.print(" > Cannot read 0 bytes");
						}
					}
					divider();
				}
			}
			/**
			 *  <> ======================================================= <>
			 * <<< SIZE COMMAND >>>
			 * This command displays the current position of the pointer (in bytes) in the filesystem
			 */
			else if(cmdParts[0].equals("size"))
			{
				System.out.println(" > This directorys file size is: " + vol.getSizeOfDirectFile() + " bytes");
				divider();
			}
			else
			{
				syntaxError();
			}
		}
	}
	
	/**
	 * @return viewEmpty - Holds a boolean value which decides to display empty data block pointers during the current session
	 */
	public static boolean getIsEmpty()
	{
		return viewEmpty;
	}
	
	/**
	 * Prints out a string telling the user they have made an error somewhere in their command syntax
	 */
	public static void syntaxError()
	{
		System.out.println("Invalid Syntax! Enter 'h' for help");
		divider();
	}
	
	/**
	 * Prints out a divider so that everything is laid out nicely in the console
	 */
	public static void divider()
	{
		System.out.format("\n<>");
		for(int i = 0; i < 35; i++)
		{
			System.out.print("-");
		}
		System.out.format("<>\n");
	}
}

import java.io.*;
import java.util.*;

// Add eng comments. 

public class Main 
{
	private static int hexCounter = 0;
	static int[][] vPage = new int [256][66000];
	static int[][] bStore = new int [256][66000];
	static int[] bStorePages = new int [256];
	
	/* Counters */
	static int pageFault = 0;
	static int pageHits = 0;
	static int loadedPages = 0;
	static int pageReplacements = 0;
	
	
	/* Variabler för att lägga in hexadecimala i rätt */
    static float pageLow = 0;
    static float pageHigh = 1;
    static int x = 0;
    
    /* Skapar en ArrayList för referance string och hex. */
    private static ArrayList<String> referanceString = new ArrayList<String>();
    private static ArrayList<String> hexValues = new ArrayList<String>();

	static Scanner sc = null;
	static Integer[][] pysMem;
	static Integer[][] pysMemCopy;
	static Integer[][] pageTable;
	
	/* Variabler för Command line arguments */
	private static String algoritm;
	private static int frames = 0;
	private static String traceFile = "";
	
	private static int pageCount[];

	public static void main(String[] args) 
	{
		/* Store Command line arguments in variables. */
		algoritm = args[1];
		frames = Integer.parseInt(args[3]);  
		traceFile = args[5];
		if(frames < 1)
		{
			System.out.println("Error! You must have at least 1 frame");
			System.exit(-1);
		}
		System.out.println(algoritm + " " + frames + " " + traceFile);
		
		pysMem = new Integer[frames][256];
		pysMemCopy = new Integer[frames][256];
		pageTable = new Integer [frames][2];
		
		loadHexToArray();
		pageCount = new int[frames];
		
		
		switch (algoritm) 
		{
		  case "fifo":
				for(int i = 0; i < referanceString.size()-1; i++)
				{
					if(loadedPages == frames)
					{
						fifo(Integer.decode(referanceString.get(i+1)), "FIFO");
					}
					else
					{
						fifo(Integer.decode(referanceString.get(i)), "FIFO");
					}
				}
				System.out.println();
				printStats("FIFO");
				break;
		    
		  case "lru":
			  
			  for(int i = 0; i < referanceString.size() - 1; i++)
			  {
				  if(loadedPages == frames)
				  {
					  lru(Integer.decode(referanceString.get(i+1)), "LRU", i);
				  }
				  
				  else
				  {
					  lru(Integer.decode(referanceString.get(i)), "LRU", i);
				  }
			  }
					
			  System.out.println();
			  printStats("LRU");
			  break;
		    
		  case "optimal":
			  
			  for(int i = 0; i < referanceString.size() - 1; i++)
			  {
				  if(loadedPages == frames)
				  {
					  optimal(Integer.decode(referanceString.get(i+1)), i); 
				  }
				  else
				  {
					  optimal(Integer.decode(referanceString.get(i)), i);
				  }
			  }
			  
			  printStats("Optimal");
			  break;
		    
		  default:
              System.out.println("Invalid choice - Please contact support");
		}
	}
	
	/* Fills upp page table with loaded pages according to the referance values.  */
	public static void loadPages(int key)
	{
		if(loadedPages < frames)
		{
			for(int i = 0; i < frames; i++)
			{
				/* If page not in pageTable, get page from backingstore */
				if(pageTable[i][0] == null)
				{
					System.out.println("Adress " + hexValues.get(hexCounter) + " not in physical memory");
					System.out.print("Page #" + bStorePages[i] + " paged in\n");
					hexCounter++;
					//System.out.println("DEN FINNS INTE!!!!"); 
					for(int k = 0; k < 256; k++)
					{
						pysMem[i][k] = bStorePages[i];
					}
					
					pageTable[i][0] = pysMem[i][i];
					pageFault++;
					loadedPages++;
					for(int l = 0; l < frames; l++)
					{
						if(pageTable[l][0] != null)
						{
							pageCount[l] = pageCount[l] + 1;
						}
					}
					
					break;
					
				}
				/* Om page finns i pageTable. */
				else if(pageTable[i][0] == key)
				{
					System.out.println("Adress " + hexValues.get(hexCounter) + " is on Page #" + bStorePages[i] + " wich is already in physical memory");
					hexCounter++;
					for(int l = 0; l < frames; l++)
					{
						if(pageTable[l][0] != null)
						{
							pageCount[l] = pageCount[l] + 1;
							
							/* Ändrar valid/invalid bit till true */
							pageTable[l][1] = 1;
						}
							
					}
					//System.out.println("DEN FINNS I PAGE TABLE!"); 
					pageHits++;
					break;
				}
			}	
		}
	}
	
	
	/* Swap the page thats not going to be used or the page thats not going to be used for the longest of time in the future. */
	public static void optimal(int key, int tracker)
	{
		int value = bStorePages[key];
		boolean lower = false;
		
		loadPages(key);
		if(loadedPages == frames)
		{
			int counter = 0;
			for(int a = 0; a < frames; a++)
			{
				if(pysMem[a][0] == key)
				{
					pageHits++;
					System.out.println("Adress " + hexValues.get(hexCounter) + " is on Page #" + bStorePages[key] + " wich is already in physical memory");
					hexCounter++;
				}
			}
			
			for(int b = 0; b < frames; b++)
			{
				//System.out.println("Counter: " + counter);
				
				/* Kollar igenom alla frames om page inte finns i pageTable.*/
				if(pageTable[b][0] != key)
				{
					counter++;
				}
				
				/* När man har kollat igenom alla pages, om den inte fanns så skall den läggas in. */
				if(counter == frames)
				{
					System.out.println("Adress " + hexValues.get(hexCounter) + " not in physical memory");
					hexCounter++;
					int farAway = 0;
					//System.out.println(referanceString);
					
					/* Sparar högsta värdet som finns i varje frame. */
					for(int k = 0; k < frames; k++)
					{
						for(int i = 0; i < referanceString.size(); i++)
						{
							if(pageTable[k][0] == Integer.decode(referanceString.get(i)))
							{
								//System.out.println(pageTable[k][0] + " finns på pos: " + i);
								farAway = i;
							}
						}
						pageCount[k] = farAway;
					}
	
					/* Kollar om det finns någon page som inte kommer att användas mer. */
					for(int l = 0; l < frames; l++)
					{
						// System.out.println("Frame #" + l + " = " + pageCount[l]);
						
						if(pageCount[l] < tracker)
						{
							lower = true;
						}
					}
					
					/* If there is a page thats is not going to be used, swap that page */
					if(lower == true)
					{
						System.out.println("Page #" + pageTable[getIndexOfMin()][0] + " paged out");
						System.out.println("Page #" + key + " paged in");
						//System.out.println("LOAD PAGE: " + key + " Remove page: " + pageTable[getIndexOfMin()][0]); 
						pageTable[getIndexOfMin()][0] = value;
						
						for(int k = 0; k < 256; k++)
                        { 
                            pysMem[getIndexOfMin()][k] = value;   
                        } 
					}
					
					/* else swap page that is not going to be used for the longest of time in the future. */
					else if(lower == false)
					{
						//System.out.println("LOAD PAGE: " + key + " Remove page: " + pageTable[getIndexOfMax()][0]);
						System.out.println("Page #" + pageTable[getIndexOfMax()][0] + " paged out");
						System.out.println("Page #" + key + " paged in");
						pageTable[getIndexOfMax()][0] = value;
						
						/* Fill up page with values from backingstore */
						for(int k = 0; k < 256; k++)
                        { 
                            pysMem[getIndexOfMax()][k] = value;   
                        } 
					}

					pageReplacements++;
                    pageFault++;
				}
			}
		}
	}
	
	
	public static void lru(int key, String algoritm, int i)
	{
		
		if(algoritm == "LRU")
		{
			loadPages(key);
			
			if(loadedPages == frames)
			{
				int counter = 0;
				for(int a = 0; a < frames; a++)
				{
					if(pysMem[a][0] == key)
					{
						System.out.println("Adress " + hexValues.get(hexCounter) + " is on Page #" + bStorePages[key] + " wich is already in physical memory");
						hexCounter++;
						//System.out.println("DEN FINNS I PAGE TABLE!"); 
						for(int l = 0; l < frames; l++)
						{
							pageCount[l] = pageCount[l] + 1;
							pageCount[a] = 0;
						} 
						pageHits++;
					}
				}
				
				for(int b = 0; b < frames; b++)
				{
					/* Kollar igenom ALLA frames om page inte finns i pageTable.*/
					if(pageTable[b][0] != key)
					{
						counter++;
					}
					
					/* If you have checked every position in pagetable. Page in and out*/
					if(counter == frames)
					{
						System.out.println("Adress " + hexValues.get(hexCounter) + " not in physical memory");
						System.out.println("Page #" + getIndexOfMax() + " paged out");
						pageReplacements++;
                        pageFault++;
                        
                        
                        /* Fill up page with values from backingstore */
                        for(int k = 0; k < 256; k++)
                        { 
                            pysMem[getIndexOfMax()][k] = bStorePages[key];     
                        }
                        
                        //Stores the first element of the array  
                        
                        pageTable[getIndexOfMax()][0] = bStorePages[key];
                        pageCount[getIndexOfMax()] = 0;
                        
						System.out.print("Page #" + bStorePages[key] + " paged in\n");
						hexCounter++;
                        break;
					}
				}
			}
			
		}
		
	}


	public static void fifo(int key, String algoritm)
	{
		if(algoritm == "FIFO")
		{
			if(loadedPages != frames)
			{
				loadPages(key);
			}
			
			if(loadedPages == frames)
			{
				int counter = 0;
				//System.out.println("FIFO");
				/* Om page finns i pageTable. */
				for(int i = 0; i < frames; i++)
				{
					if(pysMem[i][0] == key)
					{
						System.out.println("Adress " + hexValues.get(hexCounter) + " is on Page #" + bStorePages[key] + " wich is already in physical memory");
						hexCounter++;
						pageHits++;
						break;
					}
					
				}
				
				for(int i = 0; i < frames; i++)
				{
					if(pageTable[i][0] != key)
					{
						counter++;
					}
					
					/* If you have checked every position in pagetable. Page in and out*/
					if(counter == frames)
					{
						System.out.println("Adress " + hexValues.get(hexCounter) + " not in physical memory");
						System.out.println("Page #" + pageTable[0][0] + " paged out");
						hexCounter++;
						pageReplacements++;
						
						/*  */
						for(int k = 0; k < 256; k++)
						{
				            int j;
				            
				            for(j = 0; j < frames - 1; j++)
				            {  
				                //Shift element of array by one  
				            	pysMem[j][k] = pysMem[j+1][k];  
				                pysMem[j][k] = pysMem[j + 1][k];
				            }  
				            //First element of array will be added to the end  
				            pysMem[j][k] = bStorePages[key];  
				           
				           break; 
						}
						
						//System.out.println("LOAD PAGE: " + key + " Remove page: " + pageTable[0][0]); 
						pageFault++;
						
						for(int c = 0; c < 1; c++)
						{  
				      
				            for(c = 0; c < frames - 1; c++)
				            {  
				                //Shift element of array by one  
				            	pageTable[c][0] = pageTable[c+1][0];  
				            	pageTable[c][0] = pageTable[c + 1][0];
				            }  
				            //First element of array will be added to the end  
				            pageTable[c][0] = bStorePages[key]; 
				        } 
						 System.out.print("Page #" + bStorePages[key] + " paged in\n");
						 break;
					}
				}
			}
		}
	}
	
	public static void loadHexToArray()
	{
        try 
        {
        	 File file = new File("/Users/filip/Documents/Kandidatprogrammet i Datavetenkap/Operativsystem/Laboration3/Laboration3/trace.dat"); // java.io.File
             sc = new Scanner(file); // java.util.Scanner
             String line;
             while (sc.hasNextLine()) 
            {
            		line = sc.nextLine();
            		hexValues.add(line);
            		int convertedValue = Integer.decode(line);
            	
            	for(int i = 0; i < 256; i++)
            	{
            		if(convertedValue/256 >= pageLow && convertedValue/256 < pageHigh)
            		{
            			vPage[x][convertedValue] = convertedValue;
            			bStore[x][convertedValue] = vPage[x][convertedValue];
            			bStorePages[x] = x;

            			String ref = Integer.toString(x); 
            			referanceString.add(ref);
            		}
      
	            		pageHigh++;
	            		pageLow++;
	            		x++;
            	}
	            	pageHigh = 1;
	        		pageLow = 0;
	        		x = 0;
            }
          }
          catch(FileNotFoundException e)
          {
              e.printStackTrace();
          }
          finally 
          {
            if (sc != null) sc.close();
          }
        
        /* Lägger in invalid bit */
        //System.out.println("Före...");
		for(int i = 0; i < frames; i++)
        {
			pageTable[i][1] = 0;
        }
        
	}
	
	
	public static int getIndexOfMax() 
	{
	        
	        int max = pageCount[0];
	        int pos = 0;
	
	        for(int i=1; i<pageCount.length; i++) 
	        {
	            if (max < pageCount[i]) 
	            {
	                pos = i;
	                max = pageCount[i];
	            }
	        }
	        return pos;
	} 
	
	public static int getIndexOfMin() 
	{
	        
	        int min = pageCount[0];
	        int pos = 0;
	
	        for(int i=1; i<pageCount.length; i++) 
	        {
	            if (min > pageCount[i]) 
	            {
	                pos = i;
	                min = pageCount[i];
	            }
	        }
	        return pos;
	        
	} 
	
	public static void printStats(String algoritm)
	{
		System.out.println();
		System.out.println("Algorithm: " + algoritm);
		System.out.println("Frames: " + frames);
	    System.out.println("Memory accesses: " + referanceString.size());
		System.out.println("Page Hits: " + pageHits);
		System.out.println("Page Faults: " + pageFault);
		System.out.println("Page replacements: " + pageReplacements);
		System.out.println();
	}
		
}
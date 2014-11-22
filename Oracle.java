import java.io.*;
import java.math.*;



class Oracle
{
	// from day 1 to day 79 ( sep.1 (mon) to nov.18 (tue))
	// spent electricity per second
	// 86400 seconds per day 
	static int[][] realWaiting = new int[80][86400]; // real world action
	static int[][] realAction = new int[80][86400];
	static double[][] oracleWaiting = new double[80][86400]; // predicted action
	static int[][] oracleAction = new int[80][86400];   
	static int initialState;
	
	static double ALPHA = 0.5;
	 
	static final int RUNSTATE = 0;
	static final int OFFSTATE = 1;
	static final int LOGOFFSTATE = 2;
	static final int SLEEPSTATE = 3;
	
	static final double BOOT_ACTION = 8760; 
	static final double LOGIN_ACTION = 5520; 
	static final double OFF_ACTION = 3220 ; 
	
	static final double OFF_TIME = 28;
	static final double BOOT_TIME = 68;
	static final double LOGIN_TIME = 48;
	
	static final double RUN_LOOP  = 115; 
	static final double LOGOFF_LOOP = 115; 
	static final double SCREENOFF_LOOP = 80;
	static final double POWEROFF_LOOP = 1.5; 
	
	static int getAbs(int a, int b)
	{		
		return (a-1)*86400+b;
	}
	
	static double calculateOptimal(int[][] arr)
	{
		int prevState = LOGOFFSTATE; // Turing Machine Previous State
		int prevAbs = 0; // Turing Machine Previous Time
		
		double ret = 0;
		int code = 0;
		int temp=0;
		
		for(int i=1;i<80;i++)
		{
			for(int j =0 ; j<86400;j++)
			{
				code = arr[i][j]; 
				
				if(code==7001)
				{
					temp=getAbs(i,j);
					prevState=RUNSTATE;
					prevAbs = temp;
				}
				else if(code==7002)
				{
					prevState=LOGOFFSTATE;
					temp=getAbs(i,j);
					ret += RUN_LOOP*(temp-prevAbs);					
					prevAbs = temp;			
				}
				
			}
		}
		
		
		if(prevState == RUNSTATE)
		{
			temp=getAbs(79,86399);
			ret += RUN_LOOP*(temp-prevAbs);			
		}
		
		
		return ret;
	}
	
	static double calculateWorst(int[][] arr)
	{
		int prevState = LOGOFFSTATE; // Turing Machine Previous State
		int prevAbs = 0; // Turing Machine Previous Time
		
		double ret = 0;
		int code = 0;
		int temp=0;
		int screenDecide=0;
		int loginActionNum = 0;
		
		for(int i=1;i<80;i++)
		{
			for(int j =0 ; j<86400;j++)
			{
				code = arr[i][j]; 
				
				if(code==7001)
				{
					prevState = RUNSTATE; 
					temp=getAbs(i,j);
					
					screenDecide = temp-prevAbs;
					
					if(screenDecide < 600)
						ret+= screenDecide *LOGOFF_LOOP;
					else // for Screen off
						ret+= ((screenDecide-600)*SCREENOFF_LOOP) + (600 * LOGOFF_LOOP);
					
					loginActionNum ++;
					prevAbs = temp;
				}
				else if(code==7002)
				{
					prevState = LOGOFFSTATE; 
					temp=getAbs(i,j);
					ret += RUN_LOOP*(temp-prevAbs);					
					prevAbs = temp;			
				}
				
			}
		}
		
		if(prevState == LOGOFFSTATE)
		{
			temp=getAbs(79,86399);
			screenDecide = temp-prevAbs;
			
			if(screenDecide < 600)
				ret+= screenDecide *LOGOFF_LOOP;
			else // for Screen off
				ret+= ((screenDecide-600)*SCREENOFF_LOOP) + (600 * LOGOFF_LOOP);
			
		}
		else if(prevState == RUNSTATE)
		{
			temp=getAbs(79,86399);
			ret += RUN_LOOP*(temp-prevAbs);			
		}
		
		
		double actions =(loginActionNum * LOGIN_ACTION);
		actions = actions -( LOGIN_TIME * loginActionNum * LOGOFF_LOOP);
		ret += actions;
		return ret;
	}
	
	static double calculatePower(int[][] arr)
	{
		int prevState = initialState; // Turing Machine Previous State
		int prevAbs = 0; // Turing Machine Previous Time
		
		double ret = 0;
		int code = 0;
		int temp=0;
		int screenDecide=0;
		int bootActionNum = 0;
		int loginActionNum = 0;
		int offActionNum = 0;
		
		for(int i=1;i<80;i++)
		{
			for(int j =0 ; j<86400;j++)
			{
				code = arr[i][j]; 
				if(code == 12) // ON OS BOOT( OFF -> ON)
				{
					prevState = LOGOFFSTATE; 
					temp=getAbs(i,j);
					ret += POWEROFF_LOOP*(temp-prevAbs);
					bootActionNum ++;
					prevAbs = temp;
				}
				else if(code == 13)
				{
					if(prevState == LOGOFFSTATE)
					{
						prevState = OFFSTATE; 
						temp=getAbs(i,j);
						screenDecide = temp-prevAbs;
						
						if(screenDecide < 600)
							ret+= screenDecide *LOGOFF_LOOP;
						else // for Screen off
							ret+= ((screenDecide-600)*SCREENOFF_LOOP) + (600 * LOGOFF_LOOP);
						offActionNum++;
						prevAbs = temp;
					}
					else
						System.out.println("Error on calculatePower :"+ prevState);
				}	
				else if(code==7001)
				{
					prevState = RUNSTATE; 
					temp=getAbs(i,j);
					
					screenDecide = temp-prevAbs;
					
					if(screenDecide < 600)
						ret+= screenDecide *LOGOFF_LOOP;
					else // for Screen off
						ret+= ((screenDecide-600)*SCREENOFF_LOOP) + (600 * LOGOFF_LOOP);
					
					loginActionNum ++;
					prevAbs = temp;
				}
				else if(code==7002)
				{
					prevState = LOGOFFSTATE; 
					temp=getAbs(i,j);
					ret += RUN_LOOP*(temp-prevAbs);					
					prevAbs = temp;			
				}
				
			}
		}
		if(prevState == LOGOFFSTATE)
		{
			temp=getAbs(79,86399);
			screenDecide = temp-prevAbs;
			
			if(screenDecide < 600)
				ret+= screenDecide *LOGOFF_LOOP;
			else // for Screen off
				ret+= ((screenDecide-600)*SCREENOFF_LOOP) + (600 * LOGOFF_LOOP);
			
		}
		else if(prevState == RUNSTATE)
		{
			temp=getAbs(79,86399);
			ret += RUN_LOOP*(temp-prevAbs);			
		}
		else if(prevState == OFFSTATE)
		{
			temp=getAbs(79,86399);
			ret += POWEROFF_LOOP*(temp-prevAbs);
		}
		
		double actions =(loginActionNum * LOGIN_ACTION)+(offActionNum * OFF_ACTION)+(bootActionNum *BOOT_ACTION);
		actions = actions -( LOGIN_TIME * loginActionNum * LOGOFF_LOOP)- (offActionNum*LOGOFF_LOOP*OFF_TIME)- (bootActionNum * POWEROFF_LOOP * BOOT_TIME);
		ret += actions;
		return ret;
	}
	
	static int diff()
	{
		int cnt=0;
		double temp=0,temp2=0,ret=0;
		for(int i = 1;i<79;i++)//day 1 ~ day 78
		{
			for(int j =0;j<86400;j++)
			{
				temp = realWaiting[i][j];
				if(temp < threshold(temp))
				{
					temp2=oracleWaiting[i][j];
					temp -= temp2;
					ret+=Math.abs(temp);
					cnt++;
				}
					
				else
					continue;
			}
		}
		int i = (int) ret /(cnt);
		return i;
	}
	
	static int nextVal(int i, int j) //next val from realWaiting
	{
		if(i==79 && j==86399)
			return 86400;
		else
		{
			if(j==86399)
				return realWaiting[i+1][0];
			else 
				return realWaiting[i][j+1];
		}			
	}
	
	static void makeRealWaiting()
	{
		int temp=0;
		boolean isrun = false;
		
		
		for(int i=79;i>0;i--)
		{
			for(int j =86399 ; j>=0;j--)
			{
				temp = realAction[i][j];
				if(temp == 7002)
				{
					isrun = true;
				}
				else if(temp == 7001)
				{
					isrun = false;					
				}
				
				if(isrun)
					realWaiting[i][j]=0;
				else if(temp==7001)
					realWaiting[i][j]=0;
				else
					realWaiting[i][j] = nextVal(i,j)+1;
			}
		}
		
		
	}
	
	static double threshold(double val)
	{
		double ret=0;
		if(val < 600)
		{
			ret = (OFF_ACTION + BOOT_ACTION - (POWEROFF_LOOP*(BOOT_TIME+OFF_TIME)) ) / (LOGOFF_LOOP - POWEROFF_LOOP);
			
		}
		else
		{
			ret = (OFF_ACTION + BOOT_ACTION - (POWEROFF_LOOP*(BOOT_TIME+OFF_TIME))+600*(SCREENOFF_LOOP - LOGOFF_LOOP) ) / (SCREENOFF_LOOP - POWEROFF_LOOP);
		}
		
		return ret;
	}
	
	static void makeOracleAction()
	{
		int state=OFFSTATE;
		int code =0;
		double oracleVal=0;
		double thres=0;
		boolean predictOn = false;
		boolean recalculate =false;
		double promisedTime = 0;
		
		
		for(int i =1 ;i<80;i++)
		{
			for(int j =0;j<86400;j++)
			{
				code =realAction[i][j];
				if(code == 7001 || code ==7002)
					oracleAction[i][j] = code;					
			}
		}
		
		for(int i =1 ;i<80;i++)
		{
			for(int j =0;j<86400;j++)
			{
				code = oracleAction[i][j];
				if(code == 7002 || recalculate)
				{
					oracleVal = oracleWaiting[i][j];
					thres = threshold(oracleVal);
					if(oracleVal < thres)//keep going
					{
						state = LOGOFFSTATE;
						predictOn = true;
						recalculate = false;
						promisedTime = getAbs(i,j)+oracleVal;
					}
					else // power off
					{
						state= OFFSTATE;
						predictOn =false;
						recalculate =false;
						if(j==86399)
							oracleAction[i+1][0] = 13;
						else
							oracleAction[i][j+1] = 13;
					}
				}
				else if (code == 7001 && state == OFFSTATE)
				{
					state = RUNSTATE;
					predictOn=false;
					recalculate = false;
					if(j==0)
						oracleAction[i-1][86399] = 12;
					else
						oracleAction[i][j-1] = 12;
				}
				else if(state == LOGOFFSTATE && predictOn)
				{
					if (getAbs(i,j) == promisedTime)
					{
						recalculate=true;
						predictOn = false;
						promisedTime =0;
					
					}	
					else 
						continue;
					
				}	
				
				
					
			}
		}
		
	}
	
	static void makeOracleWaiting()
	{
		//copy realWaiting week 1 into oracleWaiting
		for (int i =1;i <8;i++)
		{
			for(int j =0;j<86400;j++)
				oracleWaiting[i][j]=realWaiting[i][j];
		}
		double temp=0;
		//make oracleWaiting day 8 ~ day 79
		
		for (int i =8;i <80;i++)
		{
			for(int j =0;j<86400;j++)
			{
				temp = (ALPHA * realWaiting[i-7][j]) + ((1-ALPHA) * oracleWaiting[i-7][j]);
				oracleWaiting[i][j]=temp;
			}				
		}
		
	}
	
	
	static int getDay (String s) // septempber 1 (mon) is day 1 ~ untill november 18 (tue) day 79
	{
		String[] temp = s.split(" ");
		String[] val = temp[0].split("-");
		int monthOffset =0;
		int month = Integer.parseInt(val[1]);
		int date = Integer.parseInt(val[2]);
		if(month == 10)
			monthOffset = 30;
		else if (month == 11)
			monthOffset = 61;
		else if(month == 9)
			monthOffset = 0;
		else 
			monthOffset = -1000;
		
		return monthOffset + date; //returns the day!
	}
	static int getCodeInt(String s)//array[3] value
	{
		int val = Integer.parseInt(s);
		return val;
	}
	

	static double toKwh(double val)
	{
		return val/3600000;
	}
	
	static int getTime(String s)
	{
		String[] temp = s.split(" ");
		int offset = 43200;
		String[] time;
		
		time = temp[2].split(":");
		int decide = Integer.parseInt(time[0]);
		int hour = 3600 *(Integer.parseInt(time[0]));
		int min = 60 *(Integer.parseInt(time[1]));
		int sec = Integer.parseInt(time[2]);
		
		if(temp[1].compareTo("오전") == 0)
		{
			if(decide == 12)
				return min+sec;
			else
				return hour+min+sec;
		}
		else //afternoon
		{
			if(decide ==12)
				return offset+min+sec;
			else
				return offset+hour+min+sec;				
		}
			
		//86400 is total second
	}
	static void printArray(int day, int[][] arr)
	{
			for(int j=0;j<86400;j++)
			{
				System.out.println(arr[day][j]+" ");
			}
			System.out.println();			
	}
	
	static int getInitialState(int[][] arr)
	{
		int temp=0;
		for(int i=1;i<80;i++)
		{
			for(int j =0 ; j<86400;j++)
			{
				temp = arr[i][j];
				if(temp == 12)
					return OFFSTATE;
				else if(temp == 13)
					return LOGOFFSTATE;
				else if(temp==7001)
					return LOGOFFSTATE;
				else if(temp==7002)
					return RUNSTATE;				
			}
		}
		return -1;
	}
	
	public static void main(String[] args)
	{
		String s;
		String[] temp;
		int day=0;
		int time=0;
		int code;
		
		/* Science and Technology writing class
		 * Electricity consumption saving method using future prediction
		 * case study : sofware laboratory(SNU Building 302)
		 * 
		 * 1. make realAction array correct -> get initial state
		 * 2. make realWaiting array correct
		 * 3. make calculate function
		 * 4. 
		 * 
		 * */
		try
		{	
			BufferedReader in = new BufferedReader(new FileReader(args[0]));
			BufferedWriter out = new BufferedWriter (new FileWriter("output.txt"));
			
			//loop is for
			while((s =in.readLine()) != null)
			{
			       temp = s.split("	");
			        if(temp[1].compareTo("Date and Time")==0)
			        	continue;
			       day = getDay(temp[1]);
			        if(day < 0 || day > 79)
			        	continue;
			       time =getTime(temp[1]);
			       code = getCodeInt(temp[3]);
			       
			       realAction[day][time] = code;
			        
			       
			       //out.write(String.valueOf(ret));
			       //out.newLine();
			}
			
			initialState = getInitialState(realAction);
			System.out.println("default : "+toKwh(calculateWorst(realAction))+ " kWh");
			System.out.println("real : "+toKwh(calculatePower(realAction))+ " kWh");
			makeRealWaiting();
			makeOracleWaiting();
			makeOracleAction();//9.1 to 11.18
			System.out.println("oracle : "+toKwh(calculatePower(oracleAction))+ " kWh");
			
		
			System.out.println("optimal : "+ toKwh(calculateOptimal(realAction)));
			System.out.println("diff : "+diff()+" seconds different from realworld");
	
			in.close();
			out.close();
			return;
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}


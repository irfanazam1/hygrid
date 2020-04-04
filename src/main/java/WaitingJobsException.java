class WaitingJobsException extends Exception
{
	private String exceptionMessage;
	public WaitingJobsException(String message) 
	{
		exceptionMessage=message;
	}
	public String toString()
	{
		return exceptionMessage;
	}
}
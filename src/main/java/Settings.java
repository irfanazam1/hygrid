import java.io.Serializable;
class Settings implements Serializable
{
		public String broker;
		public String security;
		public int mem;
		public int cpu;
		public int disk;
		public int wait;
		public int elapsed;
		public int man;
		
		public Settings(String address,String sec,int m,int c,int d, int w, int e,int a)
		{
			broker = address;
			security=sec;
			mem = m;
			cpu=c;
			disk=d;
			wait=w;
			elapsed=e;
			man=a;
		}
		public Settings()
		{
		}
}
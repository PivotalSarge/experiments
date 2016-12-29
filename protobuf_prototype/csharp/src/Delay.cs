using System;
using System.Threading;

namespace protobuf_prototype_csharp
{
	public class Delay
	{
		private const int maximum = 500;

		private int milliSeconds = 0;

		private Random random = new Random();

		public int Seconds
		{
			get { return 1000 * milliSeconds; }
			set { milliSeconds = value / 1000; }
		}

		public Delay()
		{
			// NOP
		}

		public void Reset()
		{
			milliSeconds = 0;
		}

		public void Randomize()
		{
			milliSeconds = random.Next(0, maximum);
		}

		public void Parse(String str)
		{
			String units = "m";
			if (0 < str.Length)
			{
				units = str.Substring(str.Length - 1);
				str.Remove(str.Length - 1);
			}

			milliSeconds = Int32.Parse(str);
			if (units == "s")
			{
				milliSeconds *= 1000;
			}
			else if (units == "u")
			{
				milliSeconds /= 1000;
			}
		}

		public void Execute()
		{
			if (maximum < milliSeconds)
			{
				Console.WriteLine("WARNING: Sleeping {0} milli-seconds...", milliSeconds);
			}
			Thread.Sleep(milliSeconds);
		}
	}
}

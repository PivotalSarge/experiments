using System;
namespace protobuf_prototype_csharp
{
	public class Shell
	{
		public Shell()
		{
			// NOP
		}

		public void RunCommandLoop(Client client)
		{
			Delay delay = new Delay();

			while (true)
			{
				if (0 < delay.Seconds)
				{
					delay.Execute();
				}
				delay.Reset();

				Console.Write("> ");
				String line = Console.ReadLine();

				string[] arguments = ParseLine(line);
				if (0 < arguments.Length)
				{
					string command = arguments[0];
					if (command == "quit")
					{
						return;
					}
					else if (command == "sleep")
					{
						if (1 < arguments.Length)
						{
							delay.Parse(arguments[1]);
						}
					}
					else if (command == "put")
					{
						if (3 < arguments.Length)
						{
							string region = arguments[1];
							string key = arguments[2];
							string value = arguments[3];
							Console.WriteLine("{0}={1}", key, value);

							client.Put(region, key, value);
							delay.Randomize();
						}
					}
					else if (command == "get")
					{
						if (2 < arguments.Length)
						{
							string region = arguments[1];
							string key = arguments[2];
							string value = client.Get(region, key);
							delay.Randomize();

							Console.WriteLine();
							Console.WriteLine("{0}={1}", key, value);
						}
					}
					else if (command == "invalidate")
					{
						if (2 < arguments.Length)
						{
							string region = arguments[1];
							string key = arguments[2];
							client.Invalidate(region, key);
							delay.Randomize();
						}
					}
					else if (command == "destroy")
					{
						if (2 < arguments.Length)
						{
							string region = arguments[1];
							string key = arguments[2];
							client.Destroy(region, key);
							delay.Randomize();
						}
					}
					else
					{
						if (command != "help")
						{
							Console.WriteLine("Unknown command: " + command);
						}
						Console.WriteLine("Valid commands:");
						Console.WriteLine("\thelp                       -- print this help message");
						Console.WriteLine("\tquit                       -- exit");
						Console.WriteLine("\tsleep <duration>           -- wait for the duration");
						Console.WriteLine("\tget <region> <key>         -- get the value for a key");
						Console.WriteLine("\tput <region> <key> <value> -- put the value for a key");
						Console.WriteLine("\tinvalidate <region> <key>  -- invalidate the key");
						Console.WriteLine("\tdestroy <region> <key>     -- destroy the key");
					}
				}
			}
		}

		private string[] ParseLine(String line)
		{
			char[] delimiterChars = { ' ', '\t' };
			return line.Split(delimiterChars);
		}
	}
}

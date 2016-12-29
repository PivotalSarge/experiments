using System;

namespace protobuf_prototype_csharp
{
	class MainClass
	{
		public static void Main(string[] args)
		{
			Client client = new Client();
			client.Connect();

			Shell shell = new Shell();
			shell.RunCommandLoop(client);

			client.Disconnect();
		}
	}
}

using System;

namespace protobuf_prototype_csharp
{
	class MainClass
	{
		public static void Main(string[] args)
		{
			Console.WriteLine("Hello, world!");

			Client client = new Client();
			client.Connect();

			client.Disconnect();

			Console.WriteLine("Goodbye, world.");
		}
	}
}

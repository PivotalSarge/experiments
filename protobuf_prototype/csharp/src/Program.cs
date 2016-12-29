﻿using System;
using System.Threading;

namespace protobuf_prototype_csharp
{
	class MainClass
	{
		public static void Main(string[] args)
		{
			Console.WriteLine("Hello, world!");

			Client client = new Client();
			client.Connect();

			Thread.Sleep(1000);

			client.Put("foo", "uno", "1");

			Thread.Sleep(1000);

			Console.WriteLine("foo=" + client.Get("foo", "uno"));

			Thread.Sleep(1000);

			client.Disconnect();

			Console.WriteLine("Goodbye, world.");
		}
	}
}

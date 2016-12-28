using System;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Threading;
using System.Text;

namespace protobuf_prototype_csharp
{
	public class Client
	{
		private const uint bufferSize = 4096;

		// The port number for the remote device.
		private const int port = 8000;

		private Socket socket = null;

		private uint id = 0;

		public Client()
		{
			// NOP
		}

		~Client()
		{
			// NOP
		}

		public bool Connected()
		{
			return socket != null && socket.Connected;
		}

		public void Connect()
		{
			if (socket == null)
			{
				try
				{
					socket = new Socket(AddressFamily.InterNetwork,
						SocketType.Stream, ProtocolType.Tcp);
					//socket.SetSocketOption(SocketOptionLevel.Socket, SocketOptionName.SendTimeout, 200);
					//socket.SetSocketOption(SocketOptionLevel.Socket, SocketOptionName.NoDelay, true);
					socket.NoDelay = true;
					socket.SendBufferSize = 8;
					socket.ReceiveBufferSize = 8;

					IPHostEntry ipHostInfo = Dns.GetHostEntry("localhost");
					IPAddress ipAddress = ipHostInfo.AddressList[0];
					IPEndPoint remoteEP = new IPEndPoint(ipAddress, port);
					socket.Connect(remoteEP);
				}
				catch (Exception e)
				{
					Console.WriteLine(e.ToString());
				}
			}

			if (Connected())
			{
				Message.ConnectRequest connectRequest = new Message.ConnectRequest();
				connectRequest.Id = 0;
				SendMessage(connectRequest);

				Message.ConnectReply connectReply = (Message.ConnectReply)ReceiveMessage();
				if (connectReply != null)
				{
					id = connectReply.Id;
				}
			}
		}

		public void Disconnect()
		{
			if (Connected())
			{
				Message.DisconnectRequest disconnectRequest = new Message.DisconnectRequest();
				disconnectRequest.Id = id;
				SendMessage(disconnectRequest);

				Message.DisconnectReply disconnectReply = (Message.DisconnectReply)ReceiveMessage();
				if (disconnectReply != null)
				{
					id = 0;
				}
			}

			if (socket != null)
			{
				try
				{
					socket.Shutdown(SocketShutdown.Both);
					socket.Close();
					socket = null;
				}
				catch (Exception e)
				{
					Console.WriteLine(e.ToString());
				}
			}
		}

		private Message.Header.Types.MessageType GetMessageType(Google.Protobuf.IMessage message)
		{
			Type t = message.GetType();
			if (t.Equals(typeof(Message.ConnectRequest)))
			{
				return Message.Header.Types.MessageType.ConnectRequest;
			}
			if (t.Equals(typeof(Message.ConnectReply)))
			{
				return Message.Header.Types.MessageType.ConnectReply;
			}
			if (t.Equals(typeof(Message.DisconnectRequest)))
			{
				return Message.Header.Types.MessageType.DisconnectRequest;
			}
			if (t.Equals(typeof(Message.DisconnectReply)))
			{
				return Message.Header.Types.MessageType.DisconnectReply;
			}
			if (t.Equals(typeof(Message.PutRequest)))
			{
				return Message.Header.Types.MessageType.PutRequest;
			}
			if (t.Equals(typeof(Message.PutReply)))
			{
				return Message.Header.Types.MessageType.PutReply;
			}
			if (t.Equals(typeof(Message.GetRequest)))
			{
				return Message.Header.Types.MessageType.GetRequest;
			}
			if (t.Equals(typeof(Message.GetReply)))
			{
				return Message.Header.Types.MessageType.GetReply;
			}
			if (t.Equals(typeof(Message.InvalidateRequest)))
			{
				return Message.Header.Types.MessageType.InvalidateRequest;
			}
			if (t.Equals(typeof(Message.InvalidateReply)))
			{
				return Message.Header.Types.MessageType.InvalidateReply;
			}
			if (t.Equals(typeof(Message.DestroyRequest)))
			{
				return Message.Header.Types.MessageType.DestroyRequest;
			}
			if (t.Equals(typeof(Message.DestroyReply)))
			{
				return Message.Header.Types.MessageType.DestroyReply;
			}
			return Message.Header.Types.MessageType.UnspecifiedType;
		}

		private Google.Protobuf.IMessage GetMessageBuilder(Message.Header.Types.MessageType messageType)
		{
			if (Message.Header.Types.MessageType.ConnectRequest == messageType)
			{
				return new Message.ConnectRequest();
			}
			if (Message.Header.Types.MessageType.ConnectReply == messageType)
			{
				return new Message.ConnectReply();
			}
			if (Message.Header.Types.MessageType.DisconnectRequest == messageType)
			{
				return new Message.DisconnectRequest();
			}
			if (Message.Header.Types.MessageType.DisconnectReply == messageType)
			{
				return new Message.DisconnectReply();
			}
			if (Message.Header.Types.MessageType.PutRequest == messageType)
			{
				return new Message.PutRequest();
			}
			if (Message.Header.Types.MessageType.PutReply == messageType)
			{
				return new Message.PutReply();
			}
			if (Message.Header.Types.MessageType.GetRequest == messageType)
			{
				return new Message.GetRequest();
			}
			if (Message.Header.Types.MessageType.GetReply == messageType)
			{
				return new Message.GetReply();
			}
			if (Message.Header.Types.MessageType.InvalidateRequest == messageType)
			{
				return new Message.InvalidateRequest();
			}
			if (Message.Header.Types.MessageType.InvalidateReply == messageType)
			{
				return new Message.InvalidateReply();
			}
			if (Message.Header.Types.MessageType.DestroyRequest == messageType)
			{
				return new Message.DestroyRequest();
			}
			if (Message.Header.Types.MessageType.DestroyReply == messageType)
			{
				return new Message.DestroyReply();
			}
			return null;
		}

		private void SendMessage(Google.Protobuf.IMessage message)
		{
			byte[] messageBytes = new byte[bufferSize];
			Google.Protobuf.CodedOutputStream messageStream = new Google.Protobuf.CodedOutputStream(messageBytes);
			message.WriteTo(messageStream);
			uint messageSize = bufferSize - (uint)messageStream.SpaceLeft;

			Message.Header header = new Message.Header();
			header.MessageType = GetMessageType(message);
			header.MessageSize = messageSize;
			header.Ok = true;

			byte[] headerBytes = new byte[bufferSize];
			Google.Protobuf.CodedOutputStream headerStream = new Google.Protobuf.CodedOutputStream(headerBytes);
			header.WriteTo(headerStream);
			uint headerSize = bufferSize - (uint)headerStream.SpaceLeft;

			NetworkStream stream = new NetworkStream(socket);
			BinaryWriter writer = new BinaryWriter(stream);

			Byte[] b = new Byte[headerSize];
			for (int i = 0; i < headerSize; ++i)
			{
				b[i] = headerBytes[i];
			}
			writer.Write(b);
			writer.Flush();

			b = new Byte[messageSize];
			for (int i = 0; i < messageSize; ++i)
			{
				b[i] = messageBytes[i];
			}
			writer.Write(b);
			writer.Flush();

			writer.Close();
			writer.Dispose();
			writer = null;

			//if (headerSize == socket.Send(b))
			//{
			//	b = new Byte[messageSize];
			//	for (int i = 0; i < messageSize; ++i)
			//	{
			//		b[i] = messageBytes[i];
			//	}
			//	if (messageSize == socket.Send(b))
			//	{
			//		// NOP
			//		Console.WriteLine("Sent header and message"); // TODO SARGE
			//	}
			//	else
			//	{
			//		Console.WriteLine("Unable to send message. :(");
			//	}
			//}
			//else
			//{
			//	Console.WriteLine("Unable to send header. :(");
			//}
		}

		private Google.Protobuf.IMessage ReceiveMessage()
		{
			NetworkStream stream = new NetworkStream(socket);
			BinaryReader reader = new BinaryReader(stream);

			byte[] headerBytes = reader.ReadBytes(9);
			if (headerBytes != null && 9 == headerBytes.Length)
			{
				Google.Protobuf.CodedInputStream headerStream = new Google.Protobuf.CodedInputStream(headerBytes);
				Message.Header header = new Message.Header();
				header.MergeFrom(headerStream);

				byte[] messageBytes = reader.ReadBytes((int) header.MessageSize);
				if (messageBytes != null && header.MessageSize == messageBytes.Length)
				{
					Google.Protobuf.CodedInputStream messageStream = new Google.Protobuf.CodedInputStream(messageBytes);
					Google.Protobuf.IMessage message = GetMessageBuilder(header.MessageType);
					message.MergeFrom(messageStream);
					return message;
				}
			}
			else
			{
				Console.WriteLine("Unable to receive header. :(");
			}

			reader.Close();
			reader.Dispose();
			reader = null;

			//Byte[] headerBytes = new Byte[9];
			//if (9 != socket.Receive(headerBytes))
			//{
			//	Console.WriteLine("Received header"); // TODO SARGE
			//										  // TODO SARGE
			//}
			//else
			//{
			//	Console.WriteLine("Unable to receive header. :(");
			//}
			return null;
		}
	}
}

using System;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Threading;
using System.Text;
using Mono;

namespace protobuf_prototype_csharp
{
	public class Client
	{
		private const uint headerSize = 9;

		private const uint bufferSize = 4096;

		// The port number for the remote device.
		private const int port = 8000;

		private Socket socket = null;

		private uint id = System.UInt32.MaxValue;

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
					//socket.SetSocketOption(SocketOptionLevel.Socket, SocketOptionName.SendTimeout, 1);
					//socket.SetSocketOption(SocketOptionLevel.Socket, SocketOptionName.NoDelay, true);
					socket.NoDelay = true;
					//socket.SendBufferSize = 1;
					//socket.ReceiveBufferSize = 1;

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
				connectRequest.Id = System.UInt32.MaxValue;
				SendMessage(connectRequest);

				Message.ConnectReply connectReply = (Message.ConnectReply)ReceiveMessage();
				if (connectReply != null)
				{
					id = connectReply.Id;
				}
				//Console.WriteLine("id={0}", id);
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

		public String Get(String region, String key)
		{
			if (Connected())
			{
				Message.GetRequest getRequest = new Message.GetRequest();
				getRequest.Region = region;
				getRequest.Key.Add(key);
				SendMessage(getRequest);

				Message.GetReply getReply = (Message.GetReply)ReceiveMessage();
				if (getReply != null)
				{
					if (0 < getReply.Pair.Count)
					{
						return getReply.Pair[0].Value;
					}
				}
			}
			return "";
		}

		public void Put(String region, String key, String value)
		{
			if (Connected())
			{
				Message.PutRequest putRequest = new Message.PutRequest();
				putRequest.Region = region;
				Message.Pair pair = new Message.Pair();
				pair.Key = key;
				pair.Value = value;
				putRequest.Pair.Add(pair);
				SendMessage(putRequest);

				Message.PutReply putReply = (Message.PutReply)ReceiveMessage();
				if (putReply != null)
				{
					//putReply.Count;
				}
			}
		}

		public void Invalidate(String region, String key)
		{
			if (Connected())
			{
				Message.InvalidateRequest invalidateRequest = new Message.InvalidateRequest();
				invalidateRequest.Region = region;
				invalidateRequest.Key.Add(key);
				SendMessage(invalidateRequest);

				Message.InvalidateReply invalidateReply = (Message.InvalidateReply)ReceiveMessage();
				if (invalidateReply != null)
				{
					//invalidateReply.Count;
				}
			}
		}

		public void Destroy(String region, String key)
		{
			if (Connected())
			{
				Message.DestroyRequest destroyRequest = new Message.DestroyRequest();
				destroyRequest.Region = region;
				destroyRequest.Key.Add(key);
				SendMessage(destroyRequest);

				Message.DestroyReply destroyReply = (Message.DestroyReply)ReceiveMessage();
				if (destroyReply != null)
				{
					//destroyReply.Count;
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

			byte[] headerBytes = new byte[headerSize];
			Google.Protobuf.CodedOutputStream headerStream = new Google.Protobuf.CodedOutputStream(headerBytes);
			header.WriteTo(headerStream);
			//uint headerSize = bufferSize - (uint)headerStream.SpaceLeft;

			socket.Blocking = false;

			Byte[] b = new Byte[headerSize];
			for (int i = 0; i < headerSize; ++i)
			{
				b[i] = headerBytes[i];
				//Console.WriteLine("HDR: b[{0}]={1:X}", i, headerBytes[i]); // TODO SARGE
			}
			if (headerSize == socket.Send(b))
			{
				b = new Byte[messageSize];
				for (int i = 0; i < messageSize; ++i)
				{
					b[i] = messageBytes[i];
					//Console.WriteLine("MSG: b[{0}]={1:X}", i, messageBytes[i]); // TODO SARGE
				}
				if (messageSize == socket.Send(b))
				{
					// NOP
				}
				else
				{
					Console.WriteLine("Unable to send message. :(");
				}
			}
			else
			{
				Console.WriteLine("Unable to send header. :(");
			}
		}

		private Google.Protobuf.IMessage ReceiveMessage()
		{
			socket.Blocking = true;

			uint expected = headerSize;
			int n = 0;
			Byte[] headerBytes = new Byte[expected];
			while (n < expected)
			{
				int m = socket.Receive(headerBytes, n, (int)expected - n, SocketFlags.None);
				//Console.WriteLine("HDR: n={0} m={1}", n, m);
				n += m;
			}
			Google.Protobuf.CodedInputStream headerStream = new Google.Protobuf.CodedInputStream(headerBytes);
			Message.Header header = new Message.Header();
			header.MergeFrom(headerStream);

			expected = header.MessageSize;
			n = 0;
			Byte[] messageBytes = new Byte[expected];
			while (n < expected)
			{
				int m = socket.Receive(messageBytes, n, (int)expected - n, SocketFlags.None);
				//Console.WriteLine("MSG: n={0} m={1}", n, m);
				n += m;
			}
			Google.Protobuf.CodedInputStream messageStream = new Google.Protobuf.CodedInputStream(messageBytes);
			Google.Protobuf.IMessage message = GetMessageBuilder(header.MessageType);
			message.MergeFrom(messageStream);

			return message;
		}
	}
}

using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Runtime.CompilerServices;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Media.Media3D;
using System.Windows.Threading;

namespace NetKeyServerGUI
{
    public class TCPNetKeyClientResponder
    {

        public int port;
        public Dictionary<int, ClientKeys> devices = new Dictionary<int, ClientKeys>();
        int nextDeviceID = 3938;
        MainWindow caller;

        public TCPNetKeyClientResponder(int port, MainWindow caller)
        {
            this.port = port;

            new Thread(TCPThread).Start();
            this.caller = caller;
        }

        public void NewDeviceConnected(ClientKeys k)
        {
            caller.Dispatcher.Invoke(delegate
            {
                caller.devicesList.Items.Add(new DeviceListItem(k));
            });
        }

        public void TCPThread()
        {
            TcpListener tcpListener = new TcpListener(IPAddress.Any, port);
            tcpListener.Start();
            while (true)
            {
                try
                {
                    using (TcpClient s = tcpListener.AcceptTcpClient())
                    {
                        NetworkStream str = s.GetStream();
                        byte[] headerbytes = new byte[6];
                        str.Read(headerbytes, 0, 6);
                        if (!headerbytes.SequenceEqual(new byte[] { 0x4e, 0x45, 0x54, 0x4b, 0x45, 0x59 }))
                        {
                            throw new Exception("Bad header");
                        }
                        byte[] sizebytes = new byte[2];
                        str.Read(sizebytes, 0, 2);
                        short size = BitConverter.ToInt16(sizebytes, 0);
                        if (size < 0 || size > 4096)
                        {
                            throw new Exception("Bad input list size (<0 or >4096)");
                        }

                        int connectionID = nextDeviceID++;
                        ClientKeys keybinds = new ClientKeys(connectionID);
                        
                        for (int x = 0; x < size; x++)
                        {
                            int index = str.ReadByte();
                            byte btnType = (byte)str.ReadByte();
                            if (index != x)
                            {
                                Console.WriteLine($"bad input id? [{index} - {x}]");
                            }
                            //keybinds.inputTypes.Add(btnType);
                            //keybinds.states.Add(0);
                            keybinds.keyBindings.Add(new KeyBinding(btnType));
                        }
                        devices.Add(connectionID, keybinds);
                        NewDeviceConnected(keybinds);

                        str.Write(new byte[] { 0x39 }, 0, 1);
                        str.Write(BitConverter.GetBytes(connectionID), 0, 4);
                    }
                } catch (Exception e)
                {
                    Console.WriteLine("Error accepting connection: " + e.Message);
                }
            }
        }
    }
}

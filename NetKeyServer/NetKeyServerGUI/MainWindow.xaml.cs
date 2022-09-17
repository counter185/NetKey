using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using System.Net;
using System.Net.Sockets;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;

namespace NetKeyServerGUI
{
    /// <summary>
    /// Logika interakcji dla klasy MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {

        [DllImport("user32.dll")]
        static extern void keybd_event(
            byte bVk,
            byte bScan,
            uint dwFlags,
            uint dwExtraInfo
        );
        const uint KEYEVENTF_KEYDOWN = 0x0000; // New definition
        const uint KEYEVENTF_KEYUP = 0x0002; //Key up flag

        public MainWindow()
        {
            InitializeComponent();

            new Thread(ThreadUDPServer).Start();
            new Thread(ThreadUpdater).Start();


            /*bindingsList.Items.Add(new InputConfigList());
            bindingsList.Items.Add(new InputConfigList());
            bindingsList.Items.Add(new InputConfigList());
            bindingsList.Items.Add(new InputConfigList());*/
        }

        protected override void OnClosed(EventArgs e)
        {
            base.OnClosed(e);
            Environment.Exit(0);
        }

        static UInt32 ReverseBytes(UInt32 value)
        {
            return (value & 0x000000FFU) << 24 | (value & 0x0000FF00U) << 8 |
                (value & 0x00FF0000U) >> 8 | (value & 0xFF000000U) >> 24;
        }

        public void AddBinding()
        {
            Dispatcher.Invoke(delegate
            {
                states.Add(false);
                bindings.Add(new InputConfigList());
                bindings[bindings.Count - 1].keyLabel.Content = "Key " + bindings.Count;
                bindingsList.Items.Add(bindings[bindings.Count - 1]);
            });
        }

        public List<bool> states = new List<bool>();
        public List<InputConfigList> bindings = new List<InputConfigList>();

        public void ThreadUDPServer()
        {
            UdpClient listener = new UdpClient(5555);
            IPEndPoint groupEP = new IPEndPoint(IPAddress.Any, 5555);

            while (true)
            {
                try
                {
                    Console.Write("Waiting for a connection... ");

                    byte[] bytes = listener.Receive(ref groupEP);
                    Console.WriteLine("Connected to " + groupEP.Address);

                    while (true)
                    {
                        //Console.WriteLine("Next frame");
                        bytes = listener.Receive(ref groupEP);
                        int inputs = (int)ReverseBytes((uint)BitConverter.ToInt32(bytes, 0));
                        //Console.WriteLine(inputs+" inputs");
                        while (inputs > states.Count)
                        {
                            AddBinding();
                        }
                        for (int i = 0; i < inputs; i++)
                        {
                            int state = BitConverter.ToInt32(bytes, 4 + 4 * i);

                            bool cKeyState = states[i];
                            if (cKeyState != (state != 0))
                            {
                                states[i] = (state != 0);
                                if (bindings[i].vKey != Keys.VirtualKeyStates.VK_NONE)
                                {
                                    keybd_event((byte)bindings[i].vKey, 0, state == 0 ? KEYEVENTF_KEYUP : KEYEVENTF_KEYDOWN, 0);
                                }
                            }
                        }
                        //Thread.Sleep(1);
                    }
                }
                catch (Exception e)
                {
                    Console.WriteLine(e.Message);
                }
            }
        }

        public void ThreadUpdater()
        {
            try
            {
                while (true)
                {
                    Dispatcher.Invoke(delegate
                    {
                        for (int x = 0; x != bindings.Count; x++)
                        {
                            bindings[x].pressedIndicator.Fill = states[x] ? Brushes.Red : Brushes.White;
                        }
                    });
                    Thread.Sleep(4);
                }
            } catch (TaskCanceledException e)
            {
                //and
            }
        }
    }
}

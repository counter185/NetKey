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

        public bool[] vKeyStates = new bool[0xFF];

        public TCPNetKeyClientResponder keyMapper;
        public int port = 5555;

        public void SetKeyState(int key, bool state)
        {
            if (vKeyStates[key] != state)
            {
                vKeyStates[key] = state;
                keybd_event((byte)key, 0, !state ? KEYEVENTF_KEYUP : KEYEVENTF_KEYDOWN, 0);
            }
        }

        public MainWindow()
        {
            InitializeComponent();

            new Thread(ThreadUDPServer).Start();
            new Thread(ThreadUpdater).Start();
            keyMapper = new TCPNetKeyClientResponder(port);

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
                states.Add(0);
                bindings.Add(new InputConfigList());
                bindings[bindings.Count - 1].keyLabel.Content = "Key " + bindings.Count;
                bindingsList.Items.Add(bindings[bindings.Count - 1]);
            });
        }

        public List<int> states = new List<int>();
        public List<InputConfigList> bindings = new List<InputConfigList>();

        public void ThreadUDPServer()
        {
            UdpClient listener = new UdpClient(port);
            IPEndPoint groupEP = new IPEndPoint(IPAddress.Any, port);

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
                        int connID = BitConverter.ToInt32(bytes, 0);
                        int inputs = BitConverter.ToInt32(bytes, 4);
                        //Console.WriteLine(inputs+" inputs: ");
                        while (inputs > states.Count)
                        {
                            AddBinding();
                        }
                        for (int i = 0; i < inputs; i++)
                        {
                            int state = BitConverter.ToInt32(bytes, 8 + 4 * i);

                            
                            int cKeyState = states[i];
                            states[i] = state;
                            if (bindings[i].inputType == 0)
                            {
                                //Console.WriteLine("Input "+i+": " + state + ", ");
                                /*
                                if (cKeyState != state)
                                {
                                    states[i] = state;
                                    if (bindings[i].vKey != Keys.VirtualKeyStates.VK_NONE)
                                    {*/
                                SetKeyState((int)bindings[i].vKey, state != 0);
                                    /*}
                                }*/
                            } else if (bindings[i].inputType == 1)
                            {
                                short slider1 = (short)((state & 0xFFFF0000) >> 16);
                                short slider2 = (short)(state & 0xFFFF);
                                //Console.WriteLine("Slider " + i + ": " + state);
                                //Console.WriteLine("Slider " + i + ": " + slider1 + ":"+slider2+", ");

                                SetKeyState((int)bindings[i].vKeyT1L, slider1 < 0);
                                SetKeyState((int)bindings[i].vKeyT2L, slider2 < 0);
                                SetKeyState((int)bindings[i].vKeyT1R, slider1 > 0);
                                SetKeyState((int)bindings[i].vKeyT2R, slider2 > 0);
                            }
                            
                        }
                        //Console.Write("\r");
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
                            if (bindings[x].inputType == 0)
                            {
                                bindings[x].pressedIndicator.Fill = states[x] != 0 ? Brushes.Red : Brushes.White;
                            } else if (bindings[x].inputType == 1)
                            {
                                short slider1 = (short)((states[x] & 0xFFFF0000) >> 16);
                                short slider2 = (short)(states[x] & 0xFFFF);

                                bindings[x].pressedIndicatorT1LSlide.Fill = (slider1 < 0) ? Brushes.Red : Brushes.White;
                                bindings[x].pressedIndicatorT1RSlide.Fill = (slider1 > 0) ? Brushes.Red : Brushes.White;
                                bindings[x].pressedIndicatorT2LSlide.Fill = (slider2 < 0) ? Brushes.Red : Brushes.White;
                                bindings[x].pressedIndicatorT2RSlide.Fill = (slider2 > 0) ? Brushes.Red : Brushes.White;
                            }
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

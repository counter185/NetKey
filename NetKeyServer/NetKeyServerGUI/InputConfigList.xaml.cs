using System;
using System.Collections.Generic;
using System.Linq;
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
using System.Runtime.InteropServices;
using System.Threading;

namespace NetKeyServerGUI
{
    /// <summary>
    /// Logika interakcji dla klasy InputConfigList.xaml
    /// </summary>
    public partial class InputConfigList : UserControl
    {
        [DllImport("user32.dll")]
        static extern short GetKeyState(Keys.VirtualKeyStates virtualKey);

        [DllImport("user32.dll", SetLastError = true)]
        [return: MarshalAs(UnmanagedType.Bool)]
        static extern bool GetKeyboardState(byte[] lpKeyState);

        public static bool KeyPressed(Keys.VirtualKeyStates a)
        {
            return (GetKeyState(a) & 0x8000) != 0;
        }

        public volatile bool awaitingKey = false;
        public volatile Keys.VirtualKeyStates vKey = Keys.VirtualKeyStates.VK_NONE;
        public void AwaitKeyThread()
        {
            while (awaitingKey)
            {
                for (int x = 1; x <= 255; x++)
                {
                    if (KeyPressed((Keys.VirtualKeyStates)x))
                    {
                        Console.WriteLine(x);
                        vKey = (Keys.VirtualKeyStates)x;
                        awaitingKey = false;
                        Dispatcher.Invoke(delegate
                        {
                            if (x >= 65 && x <= 90)
                            {
                                assignmentButton.Content = "" + (char)((int)'A' + (x - 65));
                            } else if (x >= 48 && x <= 57) {
                                assignmentButton.Content = "" + (char)((int)'0' + (x - 48));
                            } else {
                                assignmentButton.Content = vKey.ToString();
                            }
                        });
                        Thread.EndThreadAffinity();
                        break;
                    }
                }
            }
        }

        public InputConfigList()
        {
            InitializeComponent();
        }

        private void assignmentButton_Click(object sender, RoutedEventArgs e)
        {
            awaitingKey = true;
            assignmentButton.Content = "<press a key>";
            new Thread(AwaitKeyThread).Start();
        }
    }
}

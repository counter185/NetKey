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

        public volatile Button callerButton = null;
        public volatile int assiginingTo = 0;


        public volatile int inputType = 1;

        //normal button
        public volatile Keys.VirtualKeyStates vKey = Keys.VirtualKeyStates.VK_NONE;

        //slider
        public volatile Keys.VirtualKeyStates vKeyT1L, vKeyT1R, vKeyT2L, vKeyT2R = Keys.VirtualKeyStates.VK_NONE;

        public void AwaitKeyThread(object target2)
        {
            Button target = (Button)target2;
            Dispatcher.Invoke(delegate
            {
                target.Content = "<press a key>";
            });
            while (awaitingKey)
            {
                for (int x = 1; x <= 255; x++)
                {
                    if (x == (int)Keys.VirtualKeyStates.VK_SHIFT || x == (int)Keys.VirtualKeyStates.VK_CONTROL)
                    {
                        continue;
                    }
                    if (KeyPressed((Keys.VirtualKeyStates)x))
                    {
                        //Console.WriteLine(x);
                        Keys.VirtualKeyStates k = (Keys.VirtualKeyStates)x;
                        switch (assiginingTo)
                        {
                            case 1:
                                vKey = k;
                                break;
                            case 2:
                                vKeyT1L = k;
                                break;
                            case 3:
                                vKeyT1R = k;
                                break;
                            case 4:
                                vKeyT2L = k;
                                break;
                            case 5:
                                vKeyT2R = k;
                                break;
                        }
                        awaitingKey = false;
                        Dispatcher.Invoke(delegate
                        {
                            if (x >= 65 && x <= 90)
                            {
                                target.Content = "" + (char)((int)'A' + (x - 65));
                            } else if (x >= 48 && x <= 57) {
                                target.Content = "" + (char)((int)'0' + (x - 48));
                            } else {
                                target.Content = k.ToString();
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
            assiginingTo = 1;
            new Thread(AwaitKeyThread).Start(assignmentButton);
        }
        private void assignmentButtonT1L_Click(object sender, RoutedEventArgs e)
        {
            awaitingKey = true;
            assiginingTo = 2;
            new Thread(AwaitKeyThread).Start(asssignSlideT1Left);
        }
        private void assignmentButtonT1R_Click(object sender, RoutedEventArgs e)
        {
            awaitingKey = true;
            assiginingTo = 3;
            new Thread(AwaitKeyThread).Start(asssignSlideT1Right);
        }
        private void assignmentButtonT2L_Click(object sender, RoutedEventArgs e)
        {
            awaitingKey = true;
            assiginingTo = 4;
            new Thread(AwaitKeyThread).Start(asssignSlideT2Left);
        }
        private void assignmentButtonT2R_Click(object sender, RoutedEventArgs e)
        {
            awaitingKey = true;
            assiginingTo = 5;
            new Thread(AwaitKeyThread).Start(asssignSlideT2Right);
        }

        private void ComboBox_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            Console.WriteLine(inputTypeBox.SelectedIndex);
            inputType = inputTypeBox.SelectedIndex;
            switch (inputTypeBox.SelectedIndex)
            {
                case 0:
                    GridSimpleButton.Visibility = Visibility.Visible;
                    GridSlider.Visibility = Visibility.Hidden;
                    break;
                case 1:
                    GridSimpleButton.Visibility = Visibility.Hidden;
                    GridSlider.Visibility = Visibility.Visible;
                    break;
            }
        }
    }
}

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
using System.Reflection;

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

        public KeyBinding ckeys;

        //normal button
        [Obsolete]
        public volatile Keys.VirtualKeyStates vKey = Keys.VirtualKeyStates.VK_NONE;

        //slider
        [Obsolete]
        public volatile Keys.VirtualKeyStates vKeyT1L, vKeyT1R, vKeyT2L, vKeyT2R = Keys.VirtualKeyStates.VK_NONE;

        [Obsolete]
        public InputConfigList()
        {
            InitializeComponent();
        }


        public InputConfigList(KeyBinding clientKeys)
        {
            InitializeComponent();
            ckeys = clientKeys;
            AssignAllButtonsText(ckeys);
        }

        public void AssignAllButtonsText(KeyBinding keys)
        {
            for (int x = 1; x != 6; x++)
            {
                Keys.VirtualKeyStates[] kk = new Keys.VirtualKeyStates[] { 
                    keys.vKey,
                    keys.vKeyT1L,
                    keys.vKeyT1R,
                    keys.vKeyT2L,
                    keys.vKeyT2R,
                };
                AssignButton(x, (int)kk[x - 1]);
            }
        }

        public void AssignButton(int index, int key)
        {
            Keys.VirtualKeyStates k = (Keys.VirtualKeyStates)key;
            Button target = (new Button[] { assignmentButton, asssignSlideT1Left, asssignSlideT1Right, asssignSlideT2Left, asssignSlideT2Right })[index-1];
            switch (index)
            {
                case 1:
                    ckeys.vKey = k;
                    break;
                case 2:
                    ckeys.vKeyT1L = k;
                    break;
                case 3:
                    ckeys.vKeyT1R = k;
                    break;
                case 4:
                    ckeys.vKeyT2L = k;
                    break;
                case 5:
                    ckeys.vKeyT2R = k;
                    break;
            }
            awaitingKey = false;
            Dispatcher.Invoke(delegate
            {
                if (key >= 65 && key <= 90)
                {
                    target.Content = "" + (char)((int)'A' + (key - 65));
                }
                else if (key >= 48 && key <= 57)
                {
                    target.Content = "" + (char)((int)'0' + (key - 48));
                }
                else
                {
                    target.Content = k.ToString();
                }
            });
        }

        public void AwaitKeyThread(object target2)
        {
            int assigningTo = (int)target2;
            Button target = (new Button[] { assignmentButton, asssignSlideT1Left, asssignSlideT1Right, asssignSlideT2Left, asssignSlideT2Right })[assigningTo-1];
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
                        AssignButton(assigningTo, x);
                        Thread.EndThreadAffinity();
                        break;
                    }
                }
            }
        }

        private void assignmentButton_Click(object sender, RoutedEventArgs e)
        {
            awaitingKey = true;
            new Thread(AwaitKeyThread).Start(1);
        }
        private void assignmentButtonT1L_Click(object sender, RoutedEventArgs e)
        {
            awaitingKey = true;
            new Thread(AwaitKeyThread).Start(2);
        }
        private void assignmentButtonT1R_Click(object sender, RoutedEventArgs e)
        {
            awaitingKey = true;
            new Thread(AwaitKeyThread).Start(3);
        }
        private void assignmentButtonT2L_Click(object sender, RoutedEventArgs e)
        {
            awaitingKey = true;
            new Thread(AwaitKeyThread).Start(4);
        }
        private void assignmentButtonT2R_Click(object sender, RoutedEventArgs e)
        {
            awaitingKey = true;
            new Thread(AwaitKeyThread).Start(5);
        }

        private void ComboBox_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            Console.WriteLine(inputTypeBox.SelectedIndex);
            //ckeys.inputType = (byte)inputTypeBox.SelectedIndex;
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

        public void UpdateState()
        {
            if (ckeys.inputType == 1)
            {
                pressedIndicator.Fill = ckeys.state != 0 ? Brushes.Red : Brushes.White;
            }
            else if (ckeys.inputType == 2)
            {
                short slider1 = (short)((ckeys.state & 0xFFFF0000) >> 16);
                short slider2 = (short)(ckeys.state & 0xFFFF);

                pressedIndicatorT1LSlide.Fill = (slider1 < 0) ? Brushes.Red : Brushes.White;
                pressedIndicatorT1RSlide.Fill = (slider1 > 0) ? Brushes.Red : Brushes.White;
                pressedIndicatorT2LSlide.Fill = (slider2 < 0) ? Brushes.Red : Brushes.White;
                pressedIndicatorT2RSlide.Fill = (slider2 > 0) ? Brushes.Red : Brushes.White;
            }
        }
    }
}

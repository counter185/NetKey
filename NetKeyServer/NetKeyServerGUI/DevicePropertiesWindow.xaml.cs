using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Shapes;

namespace NetKeyServerGUI
{
    /// <summary>
    /// Logika interakcji dla klasy DevicePropertiesWindow.xaml
    /// </summary>
    public partial class DevicePropertiesWindow : Window
    {
        ClientKeys target;
        volatile bool windowClosed = false;

        public DevicePropertiesWindow(ClientKeys target)
        {
            this.target = target;
            InitializeComponent();
            deviceName.Content = target.deviceName;
            idtext.Content = $"ID: {target.deviceID}";
            int x = 1;
            lastTimeSeconds = DateTime.Now.Ticks / TimeSpan.TicksPerSecond;
            foreach (KeyBinding k in target.keyBindings)
            {
                if (k.inputType > 0 && k.inputType < 3)
                {
                    InputConfigList newConf = new InputConfigList(k);
                    newConf.inputTypeBox.SelectedIndex = k.inputType-1;
                    newConf.keyLabel.Content = $"Key {x++}";
                    deviceInputBindings.Items.Add(newConf);
                }
            }

            new Thread(ThreadInfoUpdater).Start();
            this.Closed += delegate
            {
                windowClosed = true;
            };
        }

        long lastTimeSeconds = 0;
        long avgCurrent = 0;
        long avgPolls = 0;

        public void ThreadInfoUpdater()
        {
            while (!windowClosed)
            {
                Dispatcher.Invoke(delegate
                {
                    long secnow = DateTime.Now.Ticks / TimeSpan.TicksPerMillisecond / 500;
                    if (secnow != lastTimeSeconds)
                    {
                        pingtext.Content = $"Latency: {target.TakeAndResetPolls()} ms";
                        lastTimeSeconds = secnow;
                    }
                    foreach (InputConfigList a in deviceInputBindings.Items)
                    {
                        a.UpdateState();
                    }
                });
                Thread.Sleep(4);
            }
        }
    }
}

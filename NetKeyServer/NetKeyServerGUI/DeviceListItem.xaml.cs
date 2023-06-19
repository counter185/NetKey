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

namespace NetKeyServerGUI
{
    /// <summary>
    /// Logika interakcji dla klasy DeviceListItem.xaml
    /// </summary>
    public partial class DeviceListItem : UserControl
    {
        public ClientKeys target;

        public DeviceListItem(ClientKeys target)
        {
            InitializeComponent();
            this.target = target;
            UpdateDeviceInfo();
            this.MouseDoubleClick += delegate
            {
                new DevicePropertiesWindow(this.target).Show();
            };
        }

        public void UpdateDeviceInfo()
        {
            deviceNameText.Content = target.deviceName;
            deviceInfoText.Content = "ID: " + target.deviceID;
        }
    }
}

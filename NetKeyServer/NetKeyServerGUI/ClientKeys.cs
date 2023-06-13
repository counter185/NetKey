using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace NetKeyServerGUI
{
    public class ClientKeys
    {
        public int deviceID;
        public List<int> states = new List<int>();
        public List<byte> inputTypes = new List<byte>();

        public ClientKeys(int deviceID)
        {
            this.deviceID = deviceID;
        }
    }
}

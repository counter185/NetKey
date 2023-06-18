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
        public string deviceName = "Unnamed device";
        public int lastLatencySum = 0;
        public int lastPolls = 0;
        public ulong lastPoll = 0;
        public List<KeyBinding> keyBindings = new List<KeyBinding>();

        public ClientKeys(int deviceID)
        {
            this.deviceID = deviceID;
        }

        public ulong TakeAndResetPolls()
        {
            ulong ret = lastPolls != 0 ? (ulong)(lastLatencySum / lastPolls) : 999;
            lastLatencySum = 0;
            lastPolls = 0;
            return ret;
        }
    }
}

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace NetKeyServerGUI
{
    public class KeyBinding
    {
        public byte inputType = 0;

        public volatile int state = 0;

        //normal button
        public Keys.VirtualKeyStates vKey = Keys.VirtualKeyStates.VK_NONE;

        //slider
        public Keys.VirtualKeyStates vKeyT1L, vKeyT1R, vKeyT2L, vKeyT2R = Keys.VirtualKeyStates.VK_NONE;

        public KeyBinding(byte intype) {
            inputType = intype;
        }
    }
}

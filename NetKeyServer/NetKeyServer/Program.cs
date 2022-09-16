using NetKeyServer;
using System.Net;
using System.Net.Sockets;
using System.Runtime.InteropServices;

[DllImport("user32.dll")]
static extern void keybd_event(
    byte bVk,
    byte bScan,
    uint dwFlags,
    uint dwExtraInfo
);
const uint KEYEVENTF_KEYDOWN = 0x0000; // New definition
const uint KEYEVENTF_KEYUP = 0x0002; //Key up flag

static UInt32 ReverseBytes(UInt32 value)
{
    return (value & 0x000000FFU) << 24 | (value & 0x0000FF00U) << 8 |
        (value & 0x00FF0000U) >> 8 | (value & 0xFF000000U) >> 24;
}

VKC.VK[] bindings = new VKC.VK[] { VKC.VK.KEY_S, VKC.VK.KEY_D, VKC.VK.KEY_F, VKC.VK.SPACE, VKC.VK.KEY_J, VKC.VK.KEY_K, VKC.VK.KEY_L};
bool[] states = new bool[bindings.Length];
for (int x = 0; x != states.Length; x++)
{
    states[x] = false;
}

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
            int inputs = (int)ReverseBytes((uint)BitConverter.ToInt32(bytes,0));
            //Console.WriteLine(inputs+" inputs");
            for (int i = 0; i < inputs; i++)
            {
                int state = BitConverter.ToInt32(bytes, 4+4*i);
                if (i < states.Length) {
                    bool cKeyState = states[i];
                    if (cKeyState != (state != 0))
                    {
                        states[i] = (state != 0);
                        keybd_event((byte)bindings[i], 0, state == 0 ? KEYEVENTF_KEYUP : KEYEVENTF_KEYDOWN, 0);
                    }
                }
                //Console.WriteLine("State of input " + i + ": " + state);
            }
        }
    }
    catch (Exception e)
    {
        Console.WriteLine(e.Message);
    }
}
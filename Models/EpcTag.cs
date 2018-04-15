using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace RFIDTest.Models
{
    public class EpcTag
    {
        public int ID { get; set; }
        public string EPC { get; set; }
        public string RSSI { get; set; }
        public int Count { get; set; }
    }
}

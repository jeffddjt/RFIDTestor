using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using RFIDTest.Models;

namespace RFIDTest.Controllers
{
    [Produces("application/json")]
    [Route("api/RFID/[action]")]
    public class RFIDController : Controller
    {
        private FXContext db;

        public RFIDController()
        {
            this.db = new FXContext();
        }
        [HttpGet]
        public List<EpcTag> GetList()
        {
            return this.db.EpcTag.OrderByDescending(p=>p.RSSI).ToList();
        }
        [HttpPost]
        public int Add(EpcTag epcTag)
        {
            if(this.db.EpcTag.Any(p=>p.EPC==epcTag.EPC))
            {
                return this.update(epcTag);                
            }
            this.db.EpcTag.Add(epcTag);
            return this.db.SaveChanges();
        }
        [HttpGet]
        public void Clear()
        {
            this.db.Clear();
        }
        private int update(EpcTag epcTag)
        {
            EpcTag tag = this.db.EpcTag.Where(p => p.EPC == epcTag.EPC).FirstOrDefault();
            tag.Count += 1;
            this.db.EpcTag.Update(tag);
            return this.db.SaveChanges();
        }
    }
}

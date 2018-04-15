using Microsoft.EntityFrameworkCore;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace RFIDTest.Models
{
    public class FXContext:DbContext
    {
        public DbSet<EpcTag> EpcTag { get; set; }
        public FXContext()
        {
            Database.Migrate();
        }
        public void Clear()
        {
            Database.ExecuteSqlCommand("truncate table EpcTag");
        }
        protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
        {
            optionsBuilder.UseSqlServer("Server=192.168.40.251;Database=RFIDTest;User ID=sa;Password=aaaa1111!");
            base.OnConfiguring(optionsBuilder);
        }
    }
}

package au.id.micolous.andprox.test;


import org.junit.Test;

import au.id.micolous.andprox.ProxmarkVersion;
import static org.junit.Assert.*;


public class VersionTest {
    @Test
    public void testOfficial() {
        String s = "bootrom: master/v2.2 2015-07-31 11:28:11\n" +
                "os: master/v3.0.1-382-gab20cc3-suspect 2018-08-01 09:37:43\n" +
                "LF FPGA image built for 2s30vq100 on 2015/03/06 at 07:38:04\n" +
                "HF FPGA image built for 2s30vq100 on 2017/10/27 at 08:30:59\n";

        ProxmarkVersion v = ProxmarkVersion.parse(s);

        assertEquals(v.getBranch(), ProxmarkVersion.Branch.OFFICIAL);

    }
}

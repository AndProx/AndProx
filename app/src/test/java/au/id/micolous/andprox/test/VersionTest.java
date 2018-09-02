package au.id.micolous.andprox.test;


import org.junit.Test;

import au.id.micolous.andprox.ProxmarkVersion;
import static org.junit.Assert.*;

/**
 * Test routines for {@link ProxmarkVersion}.
 *
 * Recommended search for finding more firmwares:
 *   site:proxmark.org os bootrom fpga
 */
public class VersionTest {
    @Test
    public void testIceman() {
        String s = " [ ARM ]\n" +
                " bootrom: master/v2.2 2015-07-31 11:28:11\n" +
                "      os: iceman/master/ice_v3.1.0-1032-g60f1610f-dirty-unclean 2018-09-02 21:41:35\n" +
                "\n" +
                " [ FPGA ]\n" +
                " LF image built for 2s30vq100 on 2017/10/25 at 19:50:50\n" +
                " HF image built for 2s30vq100 on 2018/ 8/10 at 11:48:34";

        ProxmarkVersion v = ProxmarkVersion.parse(s);

        assertNotNull(v);
        assertEquals(ProxmarkVersion.Branch.ICEMAN, v.getBranch());
        assertFalse(v.isSupportedVersion());
    }

    @Test
    public void testIcemanOld() {
        // http://www.proxmark.org/forum/viewtopic.php?pid=24508#p24508
        String s = "bootrom: iceman/master/release-build(no_git)-suspect 2016-10-08 12:39:55\n" +
                "os: iceman/master/release-build(no_git)-suspect 2016-10-13 16:17:50\n" +
                "LF FPGA image built for 2s30vq100 on 2015/03/06 at 07:38:04\n" +
                "HF FPGA image built for 2s30vq100 on 2015/11/ 2 at  9: 8: 8";

        ProxmarkVersion v = ProxmarkVersion.parse(s);

        assertNotNull(v);
        assertEquals(ProxmarkVersion.Branch.ICEMAN, v.getBranch());
        assertFalse(v.isSupportedVersion());

        s = "bootrom: iceman/master/v1.1.0-1626-g4ce2037-suspect 2016-10-17 21:10:17\n" +
                "os: iceman/master/v1.1.0-1626-g4ce2037-suspect 2016-10-17 21:11:32\n" +
                "LF FPGA image built for 2s30vq100 on 2015/03/06 at 07:38:04\n" +
                "HF FPGA image built for 2s30vq100 on 2015/11/ 2 at  9: 8: 8";

        v = ProxmarkVersion.parse(s);

        assertNotNull(v);
        assertEquals(ProxmarkVersion.Branch.ICEMAN, v.getBranch());
        assertFalse(v.isSupportedVersion());
    }

    @Test
    public void testOfficial() {
        String s = "bootrom: master/v2.2 2015-07-31 11:28:11\n" +
                "os: master/v3.0.1-382-gab20cc3-suspect 2018-08-01 09:37:43\n" +
                "LF FPGA image built for 2s30vq100 on 2015/03/06 at 07:38:04\n" +
                "HF FPGA image built for 2s30vq100 on 2017/10/27 at 08:30:59\n";

        ProxmarkVersion v = ProxmarkVersion.parse(s);

        assertNotNull(v);
        assertEquals(ProxmarkVersion.Branch.OFFICIAL, v.getBranch());
        assertTrue(v.isDirty());

        // $ TZ=UTC date --date='@1533116263.000'
        // Wed Aug  1 09:37:43 UTC 2018
        assertEquals(v.getOSBuildTime().getTimeInMillis(), 1533116263000L);
        assertEquals(3, v.getOSMajorVersion());
        assertEquals(0, v.getOSMinorVersion());
        assertEquals(1, v.getOSPatchVersion());
        assertEquals(382, v.getOSCommitCount());
        assertEquals("ab20cc3", v.getOSCommitHash());

        assertTrue(v.isSupportedVersion());
    }

    @Test
    public void testChina() {
        // http://www.proxmark.org/forum/viewtopic.php?id=4919
        String s = "bootrom: /-suspect 2016-11-09 00:59:56\n" +
                "os: /-suspect 2016-12-08 12:45:38\n" +
                "HF FPGA image built on 2015/03/09 at 08:41:42\n" +
                "  Modify by Willok(willok@163.com)\n" +
                "\n" +
                "     proxmark3.taobao.com";

        ProxmarkVersion v = ProxmarkVersion.parse(s);

        assertNotNull(v);
        assertEquals(ProxmarkVersion.Branch.CHINA, v.getBranch());
        assertFalse(v.isSupportedVersion());

        // http://www.proxmark.org/forum/viewtopic.php?id=5515
        s = "bootrom: /-suspect 2015-04-02 15:12:04          \n" +
                "os: /-suspect 2015-04-02 15:12:11          \n" +
                "HF FPGA image built on 2015/03/09 at 08:41:42    ";

        v = ProxmarkVersion.parse(s);

        assertNotNull(v);
        assertEquals(ProxmarkVersion.Branch.CHINA, v.getBranch());
        assertFalse(v.isSupportedVersion());
    }

    @Test
    public void testError() {
        // http://www.proxmark.org/forum/viewtopic.php?id=3874
        String s = "bootrom: Version information not available\n" +
                "os: Version information not available\n" +
                "LF FPGA image built for 2s30vq100 on 2015/03/06 at 07:38:04\n" +
                "HF FPGA image built for 2s30vq100 on 2015/11/ 2 at  9: 8: 8";

        ProxmarkVersion v = ProxmarkVersion.parse(s);

        assertNotNull(v);
        assertEquals(ProxmarkVersion.Branch.ERROR, v.getBranch());
        assertFalse(v.isDirty());
    }
}

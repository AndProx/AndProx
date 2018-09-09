/*
 * This file is part of AndProx, an application for using Proxmark3 on Android.
 *
 * Copyright 2016-2017 Michael Farrell <micolous+git@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Under section 7 of the GNU General Public License v3, the following additional
 * terms apply to this program:
 *
 *  (b) You must preserve reasonable legal notices and author attributions in
 *      the program.
 *  (c) You must not misrepresent the origin of this program, and need to mark
 *      modified versions in reasonable ways as different from the original
 *      version (such as changing the name and logos).
 *  (d) You may not use the names of licensors or authors for publicity
 *      purposes, without explicit written permission.
 */
package au.id.micolous.andprox.natives;

import android.content.Context;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import static au.id.micolous.andprox.natives.Natives.PM3_STORAGE_ROOT;

/**
 * File resources for Proxmark3 client.
 */

public class Resources {
    private static final String[] PM3_STANDARD_SCRIPTS = {
            "test_foobar.lua",

            "14araw.lua",
            "brutesim.lua",
            "cmdline.lua",
            "didump.lua",
            "dumptoemul.lua",
            "emul2dump.lua",
            "emul2html.lua",
            "formatMifare.lua",
            "hf_read.lua",
            "htmldump.lua",
            "lf_bulk_program.lua",
            "mfkeys.lua",
            "mifare_autopwn.lua",
            "mifarePlus.lua",
            "ndef_dump.lua",
            "parameters.lua",
            "remagic.lua",
            "test.lua",
            "test_t55x7_ask.lua",
            "test_t55x7_bi.lua",
            "test_t55x7_fsk.lua",
            "test_t55x7_psk.lua",
            "tnp3clone.lua",
            "tnp3dump.lua",
            "tnp3sim.lua",
            "tracetest.lua"
    };

    private static final String[] PM3_LUA_LIBS = {
            "commands.lua",
            "default_toys.lua",
            "getopt.lua",
            "hf_reader.lua",
            "html_dumplib.lua",
            "htmlskel.lua",
            "md5.lua",
            "mf_default_keys.lua",
            "precalc.lua",
            "read14a.lua",
            "taglib.lua",
            "utils.lua"
    };

    private static final String[] PM3_HARDNESTED_TABLES = {
            "bitflip_0_001_states.bin.z",
            "bitflip_0_003_states.bin.z",
            "bitflip_0_005_states.bin.z",
            "bitflip_0_007_states.bin.z",
            "bitflip_0_009_states.bin.z",
            "bitflip_0_00b_states.bin.z",
            "bitflip_0_00d_states.bin.z",
            "bitflip_0_00f_states.bin.z",
            "bitflip_0_010_states.bin.z",
            "bitflip_0_014_states.bin.z",
            "bitflip_0_01c_states.bin.z",
            "bitflip_0_021_states.bin.z",
            "bitflip_0_023_states.bin.z",
            "bitflip_0_025_states.bin.z",
            "bitflip_0_027_states.bin.z",
            "bitflip_0_029_states.bin.z",
            "bitflip_0_02b_states.bin.z",
            "bitflip_0_02d_states.bin.z",
            "bitflip_0_02f_states.bin.z",
            "bitflip_0_030_states.bin.z",
            "bitflip_0_034_states.bin.z",
            "bitflip_0_03c_states.bin.z",
            "bitflip_0_040_states.bin.z",
            "bitflip_0_044_states.bin.z",
            "bitflip_0_04c_states.bin.z",
            "bitflip_0_051_states.bin.z",
            "bitflip_0_053_states.bin.z",
            "bitflip_0_055_states.bin.z",
            "bitflip_0_057_states.bin.z",
            "bitflip_0_059_states.bin.z",
            "bitflip_0_05b_states.bin.z",
            "bitflip_0_05d_states.bin.z",
            "bitflip_0_05f_states.bin.z",
            "bitflip_0_064_states.bin.z",
            "bitflip_0_06c_states.bin.z",
            "bitflip_0_071_states.bin.z",
            "bitflip_0_073_states.bin.z",
            "bitflip_0_075_states.bin.z",
            "bitflip_0_077_states.bin.z",
            "bitflip_0_079_states.bin.z",
            "bitflip_0_07b_states.bin.z",
            "bitflip_0_07f_states.bin.z",
            "bitflip_0_081_states.bin.z",
            "bitflip_0_083_states.bin.z",
            "bitflip_0_085_states.bin.z",
            "bitflip_0_087_states.bin.z",
            "bitflip_0_089_states.bin.z",
            "bitflip_0_08b_states.bin.z",
            "bitflip_0_08d_states.bin.z",
            "bitflip_0_08f_states.bin.z",
            "bitflip_0_090_states.bin.z",
            "bitflip_0_094_states.bin.z",
            "bitflip_0_09c_states.bin.z",
            "bitflip_0_0a1_states.bin.z",
            "bitflip_0_0a3_states.bin.z",
            "bitflip_0_0a5_states.bin.z",
            "bitflip_0_0a7_states.bin.z",
            "bitflip_0_0a9_states.bin.z",
            "bitflip_0_0ab_states.bin.z",
            "bitflip_0_0ad_states.bin.z",
            "bitflip_0_0af_states.bin.z",
            "bitflip_0_0b0_states.bin.z",
            "bitflip_0_0b4_states.bin.z",
            "bitflip_0_0bc_states.bin.z",
            "bitflip_0_0c0_states.bin.z",
            "bitflip_0_0c4_states.bin.z",
            "bitflip_0_0cc_states.bin.z",
            "bitflip_0_0d1_states.bin.z",
            "bitflip_0_0d3_states.bin.z",
            "bitflip_0_0d5_states.bin.z",
            "bitflip_0_0d7_states.bin.z",
            "bitflip_0_0d9_states.bin.z",
            "bitflip_0_0db_states.bin.z",
            "bitflip_0_0dd_states.bin.z",
            "bitflip_0_0df_states.bin.z",
            "bitflip_0_0e4_states.bin.z",
            "bitflip_0_0ec_states.bin.z",
            "bitflip_0_0f1_states.bin.z",
            "bitflip_0_0f3_states.bin.z",
            "bitflip_0_0f5_states.bin.z",
            "bitflip_0_0f7_states.bin.z",
            "bitflip_0_0f9_states.bin.z",
            "bitflip_0_0fb_states.bin.z",
            "bitflip_0_0fd_states.bin.z",
            "bitflip_0_0ff_states.bin.z",
            "bitflip_0_104_states.bin.z",
            "bitflip_0_10c_states.bin.z",
            "bitflip_0_111_states.bin.z",
            "bitflip_0_113_states.bin.z",
            "bitflip_0_115_states.bin.z",
            "bitflip_0_117_states.bin.z",
            "bitflip_0_119_states.bin.z",
            "bitflip_0_11b_states.bin.z",
            "bitflip_0_11d_states.bin.z",
            "bitflip_0_11f_states.bin.z",
            "bitflip_0_124_states.bin.z",
            "bitflip_0_12c_states.bin.z",
            "bitflip_0_131_states.bin.z",
            "bitflip_0_133_states.bin.z",
            "bitflip_0_135_states.bin.z",
            "bitflip_0_137_states.bin.z",
            "bitflip_0_139_states.bin.z",
            "bitflip_0_13b_states.bin.z",
            "bitflip_0_13d_states.bin.z",
            "bitflip_0_13f_states.bin.z",
            "bitflip_0_141_states.bin.z",
            "bitflip_0_143_states.bin.z",
            "bitflip_0_145_states.bin.z",
            "bitflip_0_147_states.bin.z",
            "bitflip_0_149_states.bin.z",
            "bitflip_0_14b_states.bin.z",
            "bitflip_0_14d_states.bin.z",
            "bitflip_0_14f_states.bin.z",
            "bitflip_0_150_states.bin.z",
            "bitflip_0_154_states.bin.z",
            "bitflip_0_15c_states.bin.z",
            "bitflip_0_161_states.bin.z",
            "bitflip_0_163_states.bin.z",
            "bitflip_0_165_states.bin.z",
            "bitflip_0_167_states.bin.z",
            "bitflip_0_169_states.bin.z",
            "bitflip_0_16b_states.bin.z",
            "bitflip_0_16d_states.bin.z",
            "bitflip_0_16f_states.bin.z",
            "bitflip_0_170_states.bin.z",
            "bitflip_0_174_states.bin.z",
            "bitflip_0_17c_states.bin.z",
            "bitflip_0_184_states.bin.z",
            "bitflip_0_18c_states.bin.z",
            "bitflip_0_191_states.bin.z",
            "bitflip_0_193_states.bin.z",
            "bitflip_0_195_states.bin.z",
            "bitflip_0_197_states.bin.z",
            "bitflip_0_199_states.bin.z",
            "bitflip_0_19b_states.bin.z",
            "bitflip_0_19d_states.bin.z",
            "bitflip_0_19f_states.bin.z",
            "bitflip_0_1a4_states.bin.z",
            "bitflip_0_1ac_states.bin.z",
            "bitflip_0_1b1_states.bin.z",
            "bitflip_0_1b3_states.bin.z",
            "bitflip_0_1b5_states.bin.z",
            "bitflip_0_1b7_states.bin.z",
            "bitflip_0_1b9_states.bin.z",
            "bitflip_0_1bb_states.bin.z",
            "bitflip_0_1bd_states.bin.z",
            "bitflip_0_1bf_states.bin.z",
            "bitflip_0_1c1_states.bin.z",
            "bitflip_0_1c3_states.bin.z",
            "bitflip_0_1c5_states.bin.z",
            "bitflip_0_1c9_states.bin.z",
            "bitflip_0_1cb_states.bin.z",
            "bitflip_0_1d0_states.bin.z",
            "bitflip_0_1d4_states.bin.z",
            "bitflip_0_1dc_states.bin.z",
            "bitflip_0_1e1_states.bin.z",
            "bitflip_0_1e3_states.bin.z",
            "bitflip_0_1e5_states.bin.z",
            "bitflip_0_1e7_states.bin.z",
            "bitflip_0_1e9_states.bin.z",
            "bitflip_0_1eb_states.bin.z",
            "bitflip_0_1ed_states.bin.z",
            "bitflip_0_1ef_states.bin.z",
            "bitflip_0_1f0_states.bin.z",
            "bitflip_0_1f4_states.bin.z",
            "bitflip_0_1fc_states.bin.z",
            "bitflip_0_210_states.bin.z",
            "bitflip_0_225_states.bin.z",
            "bitflip_0_227_states.bin.z",
            "bitflip_0_22d_states.bin.z",
            "bitflip_0_22f_states.bin.z",
            "bitflip_0_240_states.bin.z",
            "bitflip_0_275_states.bin.z",
            "bitflip_0_277_states.bin.z",
            "bitflip_0_27f_states.bin.z",
            "bitflip_0_294_states.bin.z",
            "bitflip_0_2a1_states.bin.z",
            "bitflip_0_2a3_states.bin.z",
            "bitflip_0_2a9_states.bin.z",
            "bitflip_0_2ab_states.bin.z",
            "bitflip_0_2c4_states.bin.z",
            "bitflip_0_2f1_states.bin.z",
            "bitflip_0_2f3_states.bin.z",
            "bitflip_0_2f9_states.bin.z",
            "bitflip_0_2fb_states.bin.z",
            "bitflip_0_335_states.bin.z",
            "bitflip_0_337_states.bin.z",
            "bitflip_0_33d_states.bin.z",
            "bitflip_0_33f_states.bin.z",
            "bitflip_0_350_states.bin.z",
            "bitflip_0_365_states.bin.z",
            "bitflip_0_367_states.bin.z",
            "bitflip_0_36d_states.bin.z",
            "bitflip_0_36f_states.bin.z",
            "bitflip_0_384_states.bin.z",
            "bitflip_0_3b1_states.bin.z",
            "bitflip_0_3b3_states.bin.z",
            "bitflip_0_3b9_states.bin.z",
            "bitflip_0_3bb_states.bin.z",
            "bitflip_0_3d4_states.bin.z",
            "bitflip_0_3e1_states.bin.z",
            "bitflip_0_3e3_states.bin.z",
            "bitflip_0_3e9_states.bin.z",
            "bitflip_0_3eb_states.bin.z",
            "bitflip_1_002_states.bin.z",
            "bitflip_1_008_states.bin.z",
            "bitflip_1_00a_states.bin.z",
            "bitflip_1_012_states.bin.z",
            "bitflip_1_018_states.bin.z",
            "bitflip_1_01a_states.bin.z",
            "bitflip_1_020_states.bin.z",
            "bitflip_1_028_states.bin.z",
            "bitflip_1_02a_states.bin.z",
            "bitflip_1_02e_states.bin.z",
            "bitflip_1_032_states.bin.z",
            "bitflip_1_036_states.bin.z",
            "bitflip_1_038_states.bin.z",
            "bitflip_1_03a_states.bin.z",
            "bitflip_1_03e_states.bin.z",
            "bitflip_1_040_states.bin.z",
            "bitflip_1_042_states.bin.z",
            "bitflip_1_046_states.bin.z",
            "bitflip_1_048_states.bin.z",
            "bitflip_1_04a_states.bin.z",
            "bitflip_1_04e_states.bin.z",
            "bitflip_1_052_states.bin.z",
            "bitflip_1_056_states.bin.z",
            "bitflip_1_058_states.bin.z",
            "bitflip_1_05a_states.bin.z",
            "bitflip_1_05e_states.bin.z",
            "bitflip_1_060_states.bin.z",
            "bitflip_1_062_states.bin.z",
            "bitflip_1_066_states.bin.z",
            "bitflip_1_068_states.bin.z",
            "bitflip_1_06a_states.bin.z",
            "bitflip_1_06e_states.bin.z",
            "bitflip_1_072_states.bin.z",
            "bitflip_1_076_states.bin.z",
            "bitflip_1_078_states.bin.z",
            "bitflip_1_07a_states.bin.z",
            "bitflip_1_07e_states.bin.z",
            "bitflip_1_080_states.bin.z",
            "bitflip_1_082_states.bin.z",
            "bitflip_1_086_states.bin.z",
            "bitflip_1_088_states.bin.z",
            "bitflip_1_08a_states.bin.z",
            "bitflip_1_08e_states.bin.z",
            "bitflip_1_092_states.bin.z",
            "bitflip_1_096_states.bin.z",
            "bitflip_1_098_states.bin.z",
            "bitflip_1_09a_states.bin.z",
            "bitflip_1_09e_states.bin.z",
            "bitflip_1_0a0_states.bin.z",
            "bitflip_1_0a2_states.bin.z",
            "bitflip_1_0a6_states.bin.z",
            "bitflip_1_0a8_states.bin.z",
            "bitflip_1_0aa_states.bin.z",
            "bitflip_1_0ae_states.bin.z",
            "bitflip_1_0b2_states.bin.z",
            "bitflip_1_0b6_states.bin.z",
            "bitflip_1_0b8_states.bin.z",
            "bitflip_1_0ba_states.bin.z",
            "bitflip_1_0be_states.bin.z",
            "bitflip_1_0c0_states.bin.z",
            "bitflip_1_0c2_states.bin.z",
            "bitflip_1_0c6_states.bin.z",
            "bitflip_1_0c8_states.bin.z",
            "bitflip_1_0ca_states.bin.z",
            "bitflip_1_0ce_states.bin.z",
            "bitflip_1_0d2_states.bin.z",
            "bitflip_1_0d6_states.bin.z",
            "bitflip_1_0d8_states.bin.z",
            "bitflip_1_0da_states.bin.z",
            "bitflip_1_0de_states.bin.z",
            "bitflip_1_0e0_states.bin.z",
            "bitflip_1_0e8_states.bin.z",
            "bitflip_1_0f8_states.bin.z",
            "bitflip_1_108_states.bin.z",
            "bitflip_1_111_states.bin.z",
            "bitflip_1_113_states.bin.z",
            "bitflip_1_115_states.bin.z",
            "bitflip_1_117_states.bin.z",
            "bitflip_1_118_states.bin.z",
            "bitflip_1_11a_states.bin.z",
            "bitflip_1_11b_states.bin.z",
            "bitflip_1_120_states.bin.z",
            "bitflip_1_122_states.bin.z",
            "bitflip_1_128_states.bin.z",
            "bitflip_1_131_states.bin.z",
            "bitflip_1_135_states.bin.z",
            "bitflip_1_138_states.bin.z",
            "bitflip_1_145_states.bin.z",
            "bitflip_1_147_states.bin.z",
            "bitflip_1_148_states.bin.z",
            "bitflip_1_158_states.bin.z",
            "bitflip_1_160_states.bin.z",
            "bitflip_1_161_states.bin.z",
            "bitflip_1_163_states.bin.z",
            "bitflip_1_165_states.bin.z",
            "bitflip_1_168_states.bin.z",
            "bitflip_1_178_states.bin.z",
            "bitflip_1_180_states.bin.z",
            "bitflip_1_188_states.bin.z",
            "bitflip_1_191_states.bin.z",
            "bitflip_1_198_states.bin.z",
            "bitflip_1_199_states.bin.z",
            "bitflip_1_19d_states.bin.z",
            "bitflip_1_19f_states.bin.z",
            "bitflip_1_1a0_states.bin.z",
            "bitflip_1_1a8_states.bin.z",
            "bitflip_1_1b3_states.bin.z",
            "bitflip_1_1b5_states.bin.z",
            "bitflip_1_1b7_states.bin.z",
            "bitflip_1_1b8_states.bin.z",
            "bitflip_1_1b9_states.bin.z",
            "bitflip_1_1bd_states.bin.z",
            "bitflip_1_1c1_states.bin.z",
            "bitflip_1_1c3_states.bin.z",
            "bitflip_1_1c8_states.bin.z",
            "bitflip_1_1c9_states.bin.z",
            "bitflip_1_1cd_states.bin.z",
            "bitflip_1_1cf_states.bin.z",
            "bitflip_1_1d8_states.bin.z",
            "bitflip_1_1e0_states.bin.z",
            "bitflip_1_1e1_states.bin.z",
            "bitflip_1_1e5_states.bin.z",
            "bitflip_1_1e7_states.bin.z",
            "bitflip_1_1e8_states.bin.z",
            "bitflip_1_1e9_states.bin.z",
            "bitflip_1_1eb_states.bin.z",
            "bitflip_1_1ed_states.bin.z",
            "bitflip_1_1f8_states.bin.z",
            "bitflip_1_208_states.bin.z",
            "bitflip_1_220_states.bin.z",
            "bitflip_1_24a_states.bin.z",
            "bitflip_1_24e_states.bin.z",
            "bitflip_1_25a_states.bin.z",
            "bitflip_1_25e_states.bin.z",
            "bitflip_1_262_states.bin.z",
            "bitflip_1_266_states.bin.z",
            "bitflip_1_272_states.bin.z",
            "bitflip_1_276_states.bin.z",
            "bitflip_1_280_states.bin.z",
            "bitflip_1_2a8_states.bin.z",
            "bitflip_1_2c2_states.bin.z",
            "bitflip_1_2c6_states.bin.z",
            "bitflip_1_2d2_states.bin.z",
            "bitflip_1_2d6_states.bin.z",
            "bitflip_1_328_states.bin.z",
            "bitflip_1_388_states.bin.z",
            "bitflip_1_3a0_states.bin.z"
    };

    private static final String TAG = "Resources";

    private static boolean copyResources(Context ctx, String dir, String[] files) {
        File f = new File(PM3_STORAGE_ROOT + "/" + dir);
        if (!f.isDirectory()) {
            if (!f.mkdirs()) {
                Log.e(TAG, "Failed to mkdir PM3_STORAGE_ROOT/" + dir);
                return false;
            }
        }

        // Copy files
        InputStream in = null;
        OutputStream out = null;
        for (String fn : files) {
            f = new File(String.format(Locale.ENGLISH, "%s/%s/%s", PM3_STORAGE_ROOT, dir, fn));

            if (f.exists() && f.isFile()) {
                continue;
            }

            try {
                in = ctx.getAssets().open(dir + "/" + fn);
                out = new FileOutputStream(f);
                IOUtils.copy(in, out);
                Log.d(TAG, String.format(Locale.ENGLISH, "Copied %s", fn));
            } catch (IOException e) {
                Log.e(TAG, String.format(Locale.ENGLISH, "Error copying %s", fn), e);
                return false;
            } finally {
                IOUtils.closeQuietly(out);
                IOUtils.closeQuietly(in);
            }
        }

        return true;
    }

    /**
     * Extracts all the PM3 resources to the sdcard directory.
     * @return true on success
     */
    public static boolean extractPM3Resources(Context ctx) {
        // TODO: Implement progression callbacks.
        Log.d(TAG, "Copying resources...");

        File f = new File(PM3_STORAGE_ROOT);
        if (!f.isDirectory()) {
            if (!f.mkdir()) {
                Log.e(TAG, "Failed to mkdir PM3_STORAGE_ROOT");
                return false;
            }
        }

        if (!copyResources(ctx, "scripts", PM3_STANDARD_SCRIPTS)) {
            return false;
        }

        if (!copyResources(ctx, "lualibs", PM3_LUA_LIBS)) {
            return false;
        }

        if (!copyResources(ctx, "hardnested", new String[] {"bf_bench_data.bin"})) {
            return false;
        }

        if (!copyResources(ctx, "hardnested/tables", PM3_HARDNESTED_TABLES)) {
            return false;
        }

        Log.d(TAG, "Finished copying resources successfully");
        return true;
    }


}

/*
 * This file is part of AndProx, an application for using Proxmark3 on Android.
 *
 * Copyright 2016-2018 Michael Farrell <micolous+git@gmail.com>
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
package au.id.micolous.andprox.components;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import au.id.micolous.andprox.R;

/**
 * Fragment which displays all the license acknowledgements.
 */
public class LicenseFragment extends Fragment {

    public LicenseFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LicenseFragment.
     */
    public static LicenseFragment newInstance() {
        return new LicenseFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_license, container, false);
        TextView t = v.findViewById(R.id.tvLicenseText);

        // These strings are deliberately not localised.
        t.setText(LICENSE_INTRO);

        t.append(PROXMARK3);
        t.append(ZLIB);
        t.append(USB_SERIAL_FOR_ANDROID);
        t.append(GRAPHVIEW);

        return v;
    }

    static final String LICENSE_INTRO = "AndProx\n"
        + "Copyright 2016-2018 Michael Farrell <micolous@gmail.com> and contributors\n"
        + "\n"
        + "This program is free software: you can redistribute it and/or modify "
        + "it under the terms of the GNU General Public License as published by "
        + "the Free Software Foundation, either version 3 of the License, or "
        + "(at your option) any later version.\n"
        + "\n"
        + "This program is distributed in the hope that it will be useful, "
        + "but WITHOUT ANY WARRANTY; without even the implied warranty of "
        + "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the "
        + "GNU General Public License for more details.\n"
        + "\n"
        + "You should have received a copy of the GNU General Public License "
        + "along with this program.  If not, see <http://www.gnu.org/licenses/>.\n"
        + "\n"
        + "Under section 7 of the GNU General Public License v3, the following additional terms apply to this program:\n"
        + "\n"
        + "(b) You must preserve reasonable legal notices and author attributions in the program.\n"
        + "(c) You must not misrepresent the origin of this program, and need to mark modified versions in reasonable ways as different from the original version (such as changing the name and logos).\n"
        + "(d) You may not use the names of licensors or authors for publicity purposes, without explicit written permission.\n"
        + "\n"
        + "The source code is available at https://github.com/AndProx/AndProx/\n\n\n";

    static final String PROXMARK3 = "Proxmark3\n"
        + "Copyright 2007-2018 proxmark3 contributors\n"
        + "\n"
        + "This program is free software; you can redistribute it and/or modify "
        + "it under the terms of the GNU General Public License as published by "
        + "the Free Software Foundation; either version 2 of the License, or "
        + "(at your option) any later version.\n"
        + "\n"
        + "This program is distributed in the hope that it will be useful, "
        + "but WITHOUT ANY WARRANTY; without even the implied warranty of "
        + "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the "
        + "GNU General Public License for more details.\n"
        + "\n"
        + "You should have received a copy of the GNU General Public License "
        + "along with this program.  If not, see <http://www.gnu.org/licenses/>.\n"
        + "\n"
        + "The modified source code is available in third_party/proxmark3/ in the AndProx repository.\n\n\n";

    static final String USB_SERIAL_FOR_ANDROID = "usb-serial-for-android\n"
        + "Copyright 2011-2012 Google Inc\n"
        + "\n"
        + "This program is free software; you can redistribute it and/or modify "
        + "it under the terms of the GNU Lesser General Public License as published by "
        + "the Free Software Foundation; either version 2.1 of the License, or "
        + "(at your option) any later version.\n"
        + "\n"
        + "This program is distributed in the hope that it will be useful, "
        + "but WITHOUT ANY WARRANTY; without even the implied warranty of "
        + "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the "
        + "GNU General Public License for more details.\n"
        + "\n"
        + "You should have received a copy of the GNU General Public License "
        + "along with this program.  If not, see <http://www.gnu.org/licenses/>.\n"
        + "\n"
        + "The source code is available in third_party/usb-serial-for-android/ in the AndProx repository.\n\n\n";

    static final String ZLIB = "zlib\n" +
        "Copyright 1995-2013 Jean-loup Gailly and Mark Adler\n"+
        "\n"+
        "This software is provided 'as-is', without any express or implied " +
        "warranty.  In no event will the authors be held liable for any damages "+
        "arising from the use of this software.\n"+
        "\n"+
        "Permission is granted to anyone to use this software for any purpose, "+
        "including commercial applications, and to alter it and redistribute it "+
        "freely, subject to the following restrictions:\n"+
        "\n"+
        "  1. The origin of this software must not be misrepresented; you must not\n"+
        "     claim that you wrote the original software. If you use this software\n"+
        "     in a product, an acknowledgment in the product documentation would be\n"+
        "     appreciated but is not required.\n"+
        "  2. Altered source versions must be plainly marked as such, and must not be\n"+
        "     misrepresented as being the original software.\n"+
        "  3. This notice may not be removed or altered from any source distribution.\n"+
        "\n"+
        "Jean-loup Gailly        Mark Adler\n"+
        "jloup@gzip.org          madler@alumni.caltech.edu\n\n"
        + "The source code is available in third_party/proxmark3/zlib/ in the AndProx repository.\n\n\n";

    static final String GRAPHVIEW = "GraphView\n"
        + "Copyright 2014 Jonas Gehring\n"
        + "\n"
        + "This program is free software; you can redistribute it and/or modify "
        + "it under the terms of the GNU General Public License as published by "
        + "the Free Software Foundation; either version 2 of the License, with the "
        + "\"Linking Exception\", which can be found at the license.txt file in "
        + "third_party/GraphView/.\n"
        + "\n"
        + "This program is distributed in the hope that it will be useful, "
        + "but WITHOUT ANY WARRANTY; without even the implied warranty of "
        + "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the "
        + "GNU General Public License for more details.\n"
        + "\n"
        + "You should have received a copy of the GNU General Public License "
        + "along with this program.  If not, see <http://www.gnu.org/licenses/>.\n"
        + "\n"
        + "The modified source code is available in third_party/GraphView/ in the AndProx repository.\n\n\n";


}

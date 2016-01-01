
package cn.com.nd.momo.im.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import cn.com.nd.momo.R;
import cn.com.nd.momo.im.buss.TextViewUtil;

/**
 * 表情GridView的Adapter类，表情添加删除在这里做
 * 
 * @author Tsung Wu <tsung.bz@gmail.com>
 */
public class SmileyAdapter extends BaseAdapter {
    public final static String[][] FilterUbbs = {
            {
                    R.drawable.big_smile + "", "大笑"
            },
            {
                    R.drawable.cry + "", "泪奔"
            },
            {
                    R.drawable.misdoubt + "", "鄙视"
            },
            {
                    R.drawable.surprise + "", "惊讶"
            },
            {
                    R.drawable.beyond_endurance + "", "怒了"
            },
            {
                    R.drawable.bad_smile + "", "坏笑"
            },
            {
                    R.drawable.i_have_no_idea + "", "没办法"
            },
            {
                    R.drawable.the_devil + "", "恶魔"
            },
            {
                    R.drawable.embarrassed + "", "尴尬"
            },
            {
                    R.drawable.greeding + "", "花心"
            },
            {
                    R.drawable.just_out + "", "汗"
            },
            {
                    R.drawable.pretty_smile + "", "可爱"
            },
            {
                    R.drawable.rockn_roll + "", "摇滚"
            },
            {
                    R.drawable.shame + "", "害羞"
            },
            {
                    R.drawable.sigh + "", "失落"
            },
            {
                    R.drawable.smile + "", "微笑"
            },
            {
                    R.drawable.unbelievable + "", "无语"
            },
            {
                    R.drawable.unhappy + "", "郁闷"
            },
            {
                    R.drawable.what + "", "困惑"
            },
            {
                    R.drawable.wicked + "", "生气"
            }
    };

    public final static String[][][] EMOJI_SMILE = {
            {// face
                    {
                            "" + R.drawable.e415, "e415", "1f604"
                    },
                    {
                            R.drawable.e056 + "", "e056", "1f60a"
                    },
                    {
                            R.drawable.e057 + "", "e057", "1f603"
                    },
                    {
                            "" + R.drawable.e414, "e414", "263a"
                    },
                    {
                            "" + R.drawable.e405, "e405", "1f609"
                    },
                    {
                            "" + R.drawable.e106, "e106", "1f60d"
                    },
                    {
                            "" + R.drawable.e418, "e418", "1f618"
                    },
                    {
                            "" + R.drawable.e417, "e417", "1f61a"
                    },
                    {
                            "" + R.drawable.e40d, "e40d", "1f633"
                    },
                    {
                            "" + R.drawable.e40a, "e40a", "1f614"
                    },
                    {
                            "" + R.drawable.e404, "e404", "1f601"
                    },
                    {
                            "" + R.drawable.e105, "e105", "1f61c"
                    },
                    {
                            "" + R.drawable.e409, "e409", "1f61d"
                    },
                    {
                            "" + R.drawable.e40e, "e40e", "1f612"
                    },
                    {
                            "" + R.drawable.e402, "e402", "1f60f"
                    },
                    {
                            "" + R.drawable.e108, "e108", "1f613"
                    },
                    {
                            "" + R.drawable.e403, "e403", "1f60c"
                    },
                    {
                            R.drawable.e058 + "", "e058", "1f61e"
                    },
                    {
                            "" + R.drawable.e407, "e407", "1f616"
                    },
                    {
                            "" + R.drawable.e401, "e401", "1f625"
                    },
                    {
                            "" + R.drawable.e40f, "e40f", "1f630"
                    },
                    {
                            "" + R.drawable.e40b, "e40b", "1f628"
                    },
                    {
                            "" + R.drawable.e406, "e406", "1f623"
                    },
                    {
                            "" + R.drawable.e413, "e413", "1f622"
                    },
                    {
                            "" + R.drawable.e411, "e411", "1f62d"
                    },
                    {
                            "" + R.drawable.e412, "e412", "1f602"
                    },
                    {
                            "" + R.drawable.e410, "e410", "1f632"
                    },
                    {
                            "" + R.drawable.e107, "e107", "1f631"
                    },
                    {
                            R.drawable.e059 + "", "e059", "1f620"
                    },
                    {
                            "" + R.drawable.e416, "e416", "1f621"
                    },
                    {
                            "" + R.drawable.e408, "e408", "1f62a"
                    },
                    {
                            "" + R.drawable.e40c, "e40c", "1f637"
                    },
                    {
                            "" + R.drawable.e11a, "e11a", "1f47f"
                    },
                    {
                            "" + R.drawable.e10c, "e10c", "1f47d"
                    },
                    {
                            "" + R.drawable.e32c, "e32c", "1f49b"
                    },
                    {
                            "" + R.drawable.e32a, "e32a", "1f499"
                    },
                    {
                            "" + R.drawable.e32d, "e32d", "1f49c"
                    },
                    {
                            "" + R.drawable.e328, "e328", "1f497"
                    },
                    {
                            "" + R.drawable.e32b, "e32b", "1f49a"
                    },
                    {
                            R.drawable.e022 + "", "e022", "2764"
                    },
                    {
                            R.drawable.e023 + "", "e023", "1f494"
                    },
                    {
                            "" + R.drawable.e327, "e327", "1f493"
                    },
                    {
                            "" + R.drawable.e329, "e329", "1f498"
                    },
                    {
                            "" + R.drawable.e32e, "e32e", "2728"
                    },
                    {
                            "" + R.drawable.e32f, "e32f", "1f31f"
                    },
                    {
                            "" + R.drawable.e334, "e334", "1f4a2"
                    },
                    {
                            "" + R.drawable.e337, "e337", "2755"
                    },
                    {
                            "" + R.drawable.e336, "e336", "2754"
                    },
                    {
                            "" + R.drawable.e13c, "e13c", "1f4a4"
                    },
                    {
                            "" + R.drawable.e330, "e330", "1f4a8"
                    },
                    {
                            "" + R.drawable.e331, "e331", "1f4a6"
                    },
                    {
                            "" + R.drawable.e326, "e326", "1f3b6"
                    },
                    {
                            R.drawable.e03e + "", "e03e", "1f3b5"
                    },
                    {
                            "" + R.drawable.e11d, "e11d", "1f525"
                    },
                    {
                            R.drawable.e05a + "", "e05a", "1f4a9"
                    },
                    {
                            R.drawable.e00e + "", "e00e", "1f44d"
                    },
                    {
                            "" + R.drawable.e421, "e421", "1f44e"
                    },
                    {
                            "" + R.drawable.e420, "e420", "1f44c"
                    },
                    {
                            R.drawable.e00d + "", "e00d", "1f44a"
                    },
                    {
                            R.drawable.e010 + "", "e010", "270a"
                    },
                    {
                            R.drawable.e011 + "", "e011", "270c"
                    },
                    {
                            "" + R.drawable.e41e, "e41e", "1f44b"
                    },
                    {
                            R.drawable.e012 + "", "e012", "270b"
                    },
                    {
                            "" + R.drawable.e422, "e422", "1f450"
                    },
                    {
                            "" + R.drawable.e22e, "e22e", "1f446"
                    },
                    {
                            "" + R.drawable.e22f, "e22f", "1f447"
                    },
                    {
                            "" + R.drawable.e231, "e231", "1f449"
                    },
                    {
                            "" + R.drawable.e230, "e230", "1f448"
                    },
                    {
                            "" + R.drawable.e427, "e427", "1f64c"
                    },
                    {
                            "" + R.drawable.e41d, "e41d", "1f64f"
                    },
                    {
                            R.drawable.e00f + "", "e00f", "261d"
                    },
                    {
                            "" + R.drawable.e41f, "e41f", "1f44f"
                    },
                    {
                            "" + R.drawable.e14c, "e14c", "1f4aa"
                    },
                    {
                            "" + R.drawable.e201, "e201", "1f6b6"
                    },
                    {
                            "" + R.drawable.e115, "e115", "1f3c3"
                    },
                    {
                            "" + R.drawable.e428, "e428", "1f46b"
                    },
                    {
                            "" + R.drawable.e51f, "e51f", "1f483"
                    },
                    {
                            "" + R.drawable.e429, "e429", "1f46f"
                    },
                    {
                            "" + R.drawable.e424, "e424", "1f646"
                    },
                    {
                            "" + R.drawable.e423, "e423", "1f645"
                    },
                    {
                            "" + R.drawable.e253, "e253", "1f481"
                    },
                    {
                            "" + R.drawable.e426, "e426", "1f647"
                    },
                    {
                            "" + R.drawable.e111, "e111", "1f48f"
                    },
                    {
                            "" + R.drawable.e425, "e425", "1f491"
                    },
                    {
                            "" + R.drawable.e31e, "e31e", "1f486"
                    },
                    {
                            "" + R.drawable.e31f, "e31f", "1f487"
                    },
                    {
                            "" + R.drawable.e31d, "e31d", "1f485"
                    },
                    {
                            R.drawable.e001 + "", "e001", "1f466"
                    },
                    {
                            R.drawable.e002 + "", "e002", "1f467"
                    },
                    {
                            R.drawable.e005 + "", "e005", "1f469"
                    },
                    {
                            R.drawable.e004 + "", "e004", "1f468"
                    },
                    {
                            "" + R.drawable.e51a, "e51a", "1f476"
                    },
                    {
                            "" + R.drawable.e519, "e519", "1f475"
                    },
                    {
                            "" + R.drawable.e518, "e518", "1f474"
                    },
                    {
                            "" + R.drawable.e515, "e515", "1f471"
                    },
                    {
                            "" + R.drawable.e516, "e516", "1f472"
                    },
                    {
                            "" + R.drawable.e517, "e517", "1f473"
                    },
                    {
                            "" + R.drawable.e51b, "e51b", "1f477"
                    },
                    {
                            "" + R.drawable.e152, "e152", "1f46e"
                    },
                    {
                            R.drawable.e04e + "", "e04e", "1f47c"
                    },
                    {
                            "" + R.drawable.e51c, "e51c", "1f478"
                    },
                    {
                            "" + R.drawable.e51e, "e51e", "1f482"
                    },
                    {
                            "" + R.drawable.e11c, "e11c", "1f480"
                    },
                    {
                            "" + R.drawable.e536, "e536", "1f463"
                    },
                    {
                            R.drawable.e003 + "", "e003", "1f48b"
                    },
                    {
                            "" + R.drawable.e41c, "e41c", "1f444"
                    },
                    {
                            "" + R.drawable.e41b, "e41b", "1f442"
                    },
                    {
                            "" + R.drawable.e419, "e419", "1f440"
                    },
                    {
                            "" + R.drawable.e41a, "e41a", "1f443"
                    },

            }, {
                    // flower
                    {
                            R.drawable.e04a + "", "e04a", "2600"
                    },
                    {
                            R.drawable.e04b + "", "e04b", "2614"
                    },
                    {
                            R.drawable.e049 + "", "e049", "2601"
                    },
                    {
                            R.drawable.e048 + "", "e048", "26c4"
                    },
                    {
                            R.drawable.e04c + "", "e04c", "1f319"
                    },
                    {
                            "" + R.drawable.e13d, "e13d", "26a1"
                    },
                    {
                            "" + R.drawable.e443, "e443", "1f300"
                    },
                    {
                            "" + R.drawable.e43e, "e43e", "1f30a"
                    },
                    {
                            R.drawable.e04f + "", "e04f", "1f431"
                    },
                    {
                            R.drawable.e052 + "", "e052", "1f436"
                    },
                    {
                            R.drawable.e053 + "", "e053", "1f42d"
                    },
                    {
                            "" + R.drawable.e524, "e524", "1f439"
                    },
                    {
                            "" + R.drawable.e52c, "e52c", "1f430"
                    },
                    {
                            "" + R.drawable.e52a, "e52a", "1f43a"
                    },
                    {
                            "" + R.drawable.e531, "e531", "1f438"
                    },
                    {
                            R.drawable.e050 + "", "e050", "1f42f"
                    },
                    {
                            "" + R.drawable.e527, "e527", "1f428"
                    },
                    {
                            R.drawable.e051 + "", "e051", "1f43b"
                    },
                    {
                            "" + R.drawable.e10b, "e10b", "1f437"
                    },
                    {
                            "" + R.drawable.e52b, "e52b", "1f42e"
                    },
                    {
                            "" + R.drawable.e52f, "e52f", "1f417"
                    },
                    {
                            "" + R.drawable.e109, "e109", "1f435"
                    },
                    {
                            "" + R.drawable.e528, "e528", "1f412"
                    },
                    {
                            R.drawable.e01a + "", "e01a", "1f434"
                    },
                    {
                            "" + R.drawable.e134, "e134", "1f40e"
                    },
                    {
                            "" + R.drawable.e530, "e530", "1f42b"
                    },
                    {
                            "" + R.drawable.e529, "e529", "1f411"
                    },
                    {
                            "" + R.drawable.e526, "e526", "1f418"
                    },
                    {
                            "" + R.drawable.e52d, "e52d", "1f40d"
                    },
                    {
                            "" + R.drawable.e521, "e521", "1f426"
                    },
                    {
                            "" + R.drawable.e523, "e523", "1f424"
                    },
                    {
                            "" + R.drawable.e52e, "e52e", "1f414"
                    },
                    {
                            R.drawable.e055 + "", "e055", "1f427"
                    },
                    {
                            "" + R.drawable.e525, "e525", "1f41b"
                    },
                    {
                            "" + R.drawable.e10a, "e10a", "1f419"
                    },
                    {
                            "" + R.drawable.e522, "e522", "1f420"
                    },
                    {
                            R.drawable.e019 + "", "e019", "1f41f"
                    },
                    {
                            R.drawable.e054 + "", "e054", "1f433"
                    },
                    {
                            "" + R.drawable.e520, "e520", "1f42c"
                    },
                    {
                            "" + R.drawable.e306, "e306", "1f490"
                    },
                    {
                            R.drawable.e030 + "", "e030", "1f338"
                    },
                    {
                            "" + R.drawable.e304, "e304", "1f337"
                    },
                    {
                            "" + R.drawable.e110, "e110", "1f340"
                    },
                    {
                            R.drawable.e032 + "", "e032", "1f339"
                    },
                    {
                            "" + R.drawable.e303, "e303", "1f33a"
                    },
                    {
                            "" + R.drawable.e305, "e305", "1f33b"
                    },
                    {
                            "" + R.drawable.e118, "e118", "1f341"
                    },
                    {
                            "" + R.drawable.e447, "e447", "1f343"
                    },
                    {
                            "" + R.drawable.e119, "e119", "1f342"
                    },
                    {
                            "" + R.drawable.e307, "e307", "1f334"
                    },
                    {
                            "" + R.drawable.e308, "e308", "1f335"
                    },
                    {
                            "" + R.drawable.e444, "e444", "1f33e"
                    },
                    {
                            "" + R.drawable.e441, "e441", "1f41a"
                    },

            }, {
                    // ring
                    {
                            "" + R.drawable.e436, "e436", "1f38d"
                    },
                    {
                            "" + R.drawable.e437, "e437", "1f49d"
                    },
                    {
                            "" + R.drawable.e438, "e438", "1f38e"
                    },
                    {
                            "" + R.drawable.e43a, "e43a", "1f392"
                    },
                    {
                            "" + R.drawable.e439, "e439", "1f393"
                    },
                    {
                            "" + R.drawable.e43b, "e43b", "1f38f"
                    },
                    {
                            "" + R.drawable.e117, "e117", "1f386"
                    },
                    {
                            "" + R.drawable.e440, "e440", "1f387"
                    },
                    {
                            "" + R.drawable.e442, "e442", "1f390"
                    },
                    {
                            "" + R.drawable.e446, "e446", "1f391"
                    },
                    {
                            "" + R.drawable.e445, "e445", "1f383"
                    },
                    {
                            "" + R.drawable.e11b, "e11b", "1f47b"
                    },
                    {
                            "" + R.drawable.e448, "e448", "1f385"
                    },
                    {
                            R.drawable.e033 + "", "e033", "1f384"
                    },
                    {
                            "" + R.drawable.e112, "e112", "1f381"
                    },
                    {
                            "" + R.drawable.e325, "e325", "1f514"
                    },
                    {
                            "" + R.drawable.e312, "e312", "1f389"
                    },
                    {
                            "" + R.drawable.e310, "e310", "1f388"
                    },
                    {
                            "" + R.drawable.e126, "e126", "1f4bf"
                    },
                    {
                            "" + R.drawable.e127, "e127", "1f4c0"
                    },
                    {
                            R.drawable.e008 + "", "e008", "1f4f7"
                    },
                    {
                            R.drawable.e03d + "", "e03d", "1f3a5"
                    },
                    {
                            R.drawable.e00c + "", "e00c", "1f4bb"
                    },
                    {
                            "" + R.drawable.e12a, "e12a", "1f4fa"
                    },
                    {
                            R.drawable.e00a + "", "e00a", "1f4f1"
                    },
                    {
                            R.drawable.e00b + "", "e00b", "1f4e0"
                    },
                    {
                            R.drawable.e009 + "", "e009", "260e"
                    },
                    {
                            "" + R.drawable.e316, "e316", "1f4bd"
                    },
                    {
                            "" + R.drawable.e129, "e129", "1f4fc"
                    },
                    {
                            "" + R.drawable.e141, "e141", "1f50a"
                    },
                    {
                            "" + R.drawable.e142, "e142", "1f4e2"
                    },
                    {
                            "" + R.drawable.e317, "e317", "1f4e3"
                    },
                    {
                            "" + R.drawable.e128, "e128", "1f4fb"
                    },
                    {
                            "" + R.drawable.e14b, "e14b", "1f4e1"
                    },
                    {
                            "" + R.drawable.e211, "e211", "27bf"
                    },
                    {
                            "" + R.drawable.e114, "e114", "1f50d"
                    },
                    {
                            "" + R.drawable.e145, "e145", "1f513"
                    },
                    {
                            "" + R.drawable.e144, "e144", "1f512"
                    },
                    {
                            R.drawable.e03f + "", "e03f", "1f511"
                    },
                    {
                            "" + R.drawable.e313, "e313", "2702"
                    },
                    {
                            "" + R.drawable.e116, "e116", "1f528"
                    },
                    {
                            "" + R.drawable.e10f, "e10f", "1f4a1"
                    },
                    {
                            "" + R.drawable.e104, "e104", "1f4f2"
                    },
                    {
                            "" + R.drawable.e103, "e103", "1f4e9"
                    },
                    {
                            "" + R.drawable.e101, "e101", "1f4eb"
                    },
                    {
                            "" + R.drawable.e102, "e102", "1f4ee"
                    },
                    {
                            "" + R.drawable.e13f, "e13f", "1f6c0"
                    },
                    {
                            "" + R.drawable.e140, "e140", "1f6bd"
                    },
                    {
                            "" + R.drawable.e11f, "e11f", "1f4ba"
                    },
                    {
                            "" + R.drawable.e12f, "e12f", "1f4b0"
                    },
                    {
                            R.drawable.e031 + "", "e031", "1f531"
                    },
                    {
                            "" + R.drawable.e30e, "e30e", "1f6ac"
                    },
                    {
                            "" + R.drawable.e311, "e311", "1f4a3"
                    },
                    {
                            "" + R.drawable.e113, "e113", "1f52b"
                    },
                    {
                            "" + R.drawable.e30f, "e30f", "1f48a"
                    },
                    {
                            "" + R.drawable.e13b, "e13b", "1f489"
                    },
                    {
                            "" + R.drawable.e42b, "e42b", "1f3c8"
                    },
                    {
                            "" + R.drawable.e42a, "e42a", "1f3c0"
                    },
                    {
                            R.drawable.e018 + "", "e018", "26bd"
                    },
                    {
                            R.drawable.e016 + "", "e016", "26be"
                    },
                    {
                            R.drawable.e015 + "", "e015", "1f3be"
                    },
                    {
                            R.drawable.e014 + "", "e013", "26f3"
                    },
                    {
                            "" + R.drawable.e42c, "e42c", "1f3b1"
                    },
                    {
                            "" + R.drawable.e42d, "e42d", "1f3ca"
                    },
                    {
                            R.drawable.e017 + "", "e017", "1f3c4"
                    },
                    {
                            R.drawable.e013 + "", "e013", "1f3bf"
                    },
                    {
                            "" + R.drawable.e20e, "e20e", "2660"
                    },
                    {
                            "" + R.drawable.e20c, "e20c", "2665"
                    },
                    {
                            "" + R.drawable.e20f, "e20f", "2663"
                    },
                    {
                            "" + R.drawable.e20d, "e20d", "2666"
                    },
                    {
                            "" + R.drawable.e131, "e131", "1f3c6"
                    },
                    {
                            "" + R.drawable.e12b, "e12b", "1f47e"
                    },
                    {
                            "" + R.drawable.e130, "e130", "1f3af"
                    },
                    {
                            "" + R.drawable.e12d, "e12d", "1f004"
                    },
                    {
                            "" + R.drawable.e324, "e324", "1f3ac"
                    },
                    {
                            "" + R.drawable.e301, "e301", "1f4dd"
                    },
                    {
                            "" + R.drawable.e148, "e148", "1f4d3"
                    },
                    {
                            "" + R.drawable.e502, "e502", "1f3a8"
                    },
                    {
                            R.drawable.e03c + "", "e03c", "1f3a4"
                    },
                    {
                            "" + R.drawable.e30a, "e30a", "1f3a7"
                    },
                    {
                            R.drawable.e040 + "", "e040", "1f3b7"
                    },
                    {
                            R.drawable.e042 + "", "e042", "1f3ba"
                    },
                    {
                            R.drawable.e041 + "", "e041", "1f3b8"
                    },
                    {
                            "" + R.drawable.e12c, "e12c", "303d"
                    },
                    {
                            R.drawable.e007 + "", "e007", "1f45f"
                    },
                    {
                            "" + R.drawable.e31a, "e31a", "1f461"
                    },
                    {
                            "" + R.drawable.e13e, "e13e", "1f460"
                    },
                    {
                            "" + R.drawable.e31b, "e31b", "1f462"
                    },
                    {
                            R.drawable.e006 + "", "e006", "1f455"
                    },
                    {
                            "" + R.drawable.e302, "e302", "1f454"
                    },
                    {
                            "" + R.drawable.e319, "e319", "1f457"
                    },
                    {
                            "" + R.drawable.e321, "e321", "1f458"
                    },
                    {
                            "" + R.drawable.e322, "e322", "1f459"
                    },
                    {
                            "" + R.drawable.e314, "e314", "1f380"
                    },
                    {
                            "" + R.drawable.e503, "e503", "1f3a9"
                    },
                    {
                            "" + R.drawable.e10e, "e10e", "1f451"
                    },
                    {
                            "" + R.drawable.e318, "e318", "1f452"
                    },
                    {
                            "" + R.drawable.e43c, "e43c", "1f302"
                    },
                    {
                            "" + R.drawable.e11e, "e11e", "1f4bc"
                    },
                    {
                            "" + R.drawable.e323, "e323", "1f45c"
                    },
                    {
                            "" + R.drawable.e31c, "e31c", "1f484"
                    },
                    {
                            R.drawable.e034 + "", "e034", "1f48d"
                    },
                    {
                            R.drawable.e035 + "", "e035", "1f48e"
                    },
                    {
                            R.drawable.e045 + "", "e045", "2615"
                    },
                    {
                            "" + R.drawable.e338, "e338", "1f375"
                    },
                    {
                            R.drawable.e047 + "", "e047", "1f37a"
                    },
                    {
                            "" + R.drawable.e30c, "e30c", "1f37b"
                    },
                    {
                            R.drawable.e044 + "", "e044", "1f378"
                    },
                    {
                            "" + R.drawable.e30b, "e30b", "1f376"
                    },
                    {
                            R.drawable.e043 + "", "e043", "1f374"
                    },
                    {
                            "" + R.drawable.e120, "e120", "1f354"
                    },
                    {
                            "" + R.drawable.e33b, "e33b", "1f35f"
                    },
                    {
                            "" + R.drawable.e33f, "e33f", "1f35d"
                    },
                    {
                            "" + R.drawable.e341, "e341", "1f35b"
                    },
                    {
                            "" + R.drawable.e34c, "e34c", "1f371"
                    },
                    {
                            "" + R.drawable.e344, "e344", "1f363"
                    },
                    {
                            "" + R.drawable.e342, "e342", "1f359"
                    },
                    {
                            "" + R.drawable.e33d, "e33d", "1f358"
                    },
                    {
                            "" + R.drawable.e33e, "e33e", "1f35a"
                    },
                    {
                            "" + R.drawable.e340, "e340", "1f35c"
                    },
                    {
                            "" + R.drawable.e34d, "e34d", "1f372"
                    },
                    {
                            "" + R.drawable.e339, "e339", "1f35e"
                    },
                    {
                            "" + R.drawable.e147, "e147", "1f373"
                    },
                    {
                            "" + R.drawable.e343, "e343", "1f362"
                    },
                    {
                            "" + R.drawable.e33c, "e33c", "1f361"
                    },
                    {
                            "" + R.drawable.e33a, "e33a", "1f366"
                    },
                    {
                            "" + R.drawable.e43f, "e43f", "1f367"
                    },
                    {
                            "" + R.drawable.e34b, "e34b", "1f382"
                    },
                    {
                            R.drawable.e046 + "", "e046", "1f370"
                    },
                    {
                            "" + R.drawable.e345, "e345", "1f34e"
                    },
                    {
                            "" + R.drawable.e346, "e346", "1f34a"
                    },
                    {
                            "" + R.drawable.e348, "e348", "1f349"
                    },
                    {
                            "" + R.drawable.e347, "e347", "1f353"
                    },
                    {
                            "" + R.drawable.e34a, "e34a", "1f346"
                    },
                    {
                            "" + R.drawable.e349, "e349", "1f345"
                    },
            }, {
                    // car
                    {
                            R.drawable.e036 + "", "e036", "1f3e0"
                    },
                    {
                            "" + R.drawable.e157, "e157", "1f3eb"
                    },
                    {
                            R.drawable.e038 + "", "e038", "1f3e2"
                    },
                    {
                            "" + R.drawable.e153, "e153", "1f3e3"
                    },
                    {
                            "" + R.drawable.e155, "e155", "1f3e5"
                    },
                    {
                            "" + R.drawable.e14d, "e14d", "1f3e6"
                    },
                    {
                            "" + R.drawable.e156, "e156", "1f3ea"
                    },
                    {
                            "" + R.drawable.e501, "e501", "1f3e9"
                    },
                    {
                            "" + R.drawable.e158, "e158", "1f3e8"
                    },
                    {
                            "" + R.drawable.e43d, "e43d", "1f492"
                    },
                    {
                            R.drawable.e037 + "", "e037", "26ea"
                    },
                    {
                            "" + R.drawable.e504, "e504", "1f3ec"
                    },
                    {
                            "" + R.drawable.e44a, "e44a", "1f307"
                    },
                    {
                            "" + R.drawable.e146, "e146", "1f306"
                    },
                    {
                            "" + R.drawable.e50a, "e50a", "1f52b"
                    },
                    {
                            "" + R.drawable.e505, "e505", "1f3ef"
                    },
                    {
                            "" + R.drawable.e506, "e506", "1f3f0"
                    },
                    {
                            "" + R.drawable.e122, "e122", "26fa"
                    },
                    {
                            "" + R.drawable.e508, "e508", "1f3ed"
                    },
                    {
                            "" + R.drawable.e509, "e509", "1f5fc"
                    },
                    {
                            R.drawable.e03b + "", "e03b", "1f5fb"
                    },
                    {
                            R.drawable.e04d + "", "e04d", "1f304"
                    },
                    {
                            "" + R.drawable.e449, "e449", "1f305"
                    },
                    {
                            "" + R.drawable.e44b, "e44b", "1f303"
                    },
                    {
                            "" + R.drawable.e51d, "e51d", "1f5fd"
                    },
                    {
                            "" + R.drawable.e44c, "e44c", "1f308"
                    },
                    {
                            "" + R.drawable.e124, "e124", "1f3a1"
                    },
                    {
                            "" + R.drawable.e121, "e121", "26f2"
                    },
                    {
                            "" + R.drawable.e433, "e433", "1f3a2"
                    },
                    {
                            "" + R.drawable.e202, "e202", "1f6a2"
                    },
                    {
                            "" + R.drawable.e135, "e135", "1f6a4"
                    },
                    {
                            R.drawable.e01c + "", "e01c", "26f5"
                    },
                    {
                            R.drawable.e01d + "", "e01d", "2708"
                    },
                    {
                            R.drawable.e10d + "", "e10d", "1f680"
                    },
                    {
                            "" + R.drawable.e136, "e136", "1f6b2"
                    },
                    {
                            "" + R.drawable.e42e, "e42e", "1f699"
                    },
                    {
                            R.drawable.e01b + "", "e01b", "1f697"
                    },
                    {
                            "" + R.drawable.e15a, "e15a", "1f695"
                    },
                    {
                            "" + R.drawable.e159, "e159", "1f68c"
                    },
                    {
                            "" + R.drawable.e432, "e432", "1f693"
                    },
                    {
                            "" + R.drawable.e430, "e430", "1f692"
                    },
                    {
                            "" + R.drawable.e431, "e431", "1f691"
                    },
                    {
                            "" + R.drawable.e42f, "e42f", "1f69a"
                    },
                    {
                            R.drawable.e01e + "", "e01e", "1f683"
                    },
                    {
                            R.drawable.e039 + "", "e039", "1f689"
                    },
                    {
                            "" + R.drawable.e435, "e435", "1f684"
                    },
                    {
                            R.drawable.e01f + "", "e01f", "1f685"
                    },
                    {
                            "" + R.drawable.e125, "e125", "1f3ab"
                    },
                    {
                            R.drawable.e03a + "", "e03a", "26fd"
                    },
                    {
                            "" + R.drawable.e14e, "e14e", "1f6a5"
                    },
                    {
                            "" + R.drawable.e252, "e252", "26a0"
                    },
                    {
                            "" + R.drawable.e137, "e137", "1f6a7"
                    },
                    {
                            "" + R.drawable.e209, "e209", "1f530"
                    },
                    {
                            "" + R.drawable.e154, "e154", "1f3e7"
                    },
                    {
                            "" + R.drawable.e133, "e133", "1f3b0"
                    },
                    {
                            "" + R.drawable.e150, "e150", "1f68f"
                    },
                    {
                            "" + R.drawable.e320, "e320", "1f488"
                    },
                    {
                            "" + R.drawable.e123, "e123", "2668"
                    },
                    {
                            "" + R.drawable.e132, "e132", "1f3c1"
                    },
                    {
                            "" + R.drawable.e143, "e143", "1f38c"
                    },
                    {
                            "" + R.drawable.e50b, "e50b", "1f486"
                    },
                    {
                            "" + R.drawable.e514, "e514", "1f1f0"
                    },
                    {
                            "" + R.drawable.e513, "e513", "1f340"
                    },
                    {
                            "" + R.drawable.e50c, "e50c", "1f1fa"
                    },
                    {
                            "" + R.drawable.e50d, "e50d", "1f45a"
                    },
                    {
                            "" + R.drawable.e511, "e511", "1f4e2"
                    },
                    {
                            "" + R.drawable.e50f, "e50f", "1f1ee"
                    },
                    {
                            "" + R.drawable.e512, "e512", "1f514"
                    },
                    {
                            "" + R.drawable.e510, "e510", "1f489"
                    },
                    {
                            "" + R.drawable.e50e, "e50e", "1f1e9"
                    },
            }, {
                    // sign
                    {
                            "" + R.drawable.e21c, "e21c", "0031"
                    },
                    {
                            "" + R.drawable.e21d, "e21d", "0032"
                    },
                    {
                            "" + R.drawable.e21e, "e21e", "0033"
                    },
                    {
                            "" + R.drawable.e21f, "e21f", "0034"
                    },
                    {
                            "" + R.drawable.e220, "e220", "0035"
                    },
                    {
                            "" + R.drawable.e221, "e221", "0036"
                    },
                    {
                            "" + R.drawable.e222, "e222", "0037"
                    },
                    {
                            "" + R.drawable.e223, "e223", "0038"
                    },
                    {
                            "" + R.drawable.e224, "e224", "0039"
                    },
                    {
                            "" + R.drawable.e225, "e225", "0030"
                    },
                    {
                            "" + R.drawable.e210, "e210", "0023"
                    },
                    {
                            "" + R.drawable.e232, "e232", "2b06"
                    },
                    {
                            "" + R.drawable.e233, "e233", "2b07"
                    },
                    {
                            "" + R.drawable.e235, "e235", "2b05"
                    },
                    {
                            "" + R.drawable.e234, "e234", "27a1"
                    },
                    {
                            "" + R.drawable.e236, "e236", "2197"
                    },
                    {
                            "" + R.drawable.e237, "e237", "2196"
                    },
                    {
                            "" + R.drawable.e238, "e238", "2198"
                    },
                    {
                            "" + R.drawable.e239, "e239", "2199"
                    },
                    {
                            "" + R.drawable.e23b, "e23b", "25c0"
                    },
                    {
                            "" + R.drawable.e23a, "e23a", "25b6"
                    },
                    {
                            "" + R.drawable.e23d, "e23d", "23ea"
                    },
                    {
                            "" + R.drawable.e23c, "e23c", "23e9"
                    },
                    {
                            "" + R.drawable.e24d, "e24d", "1f197"
                    },
                    {
                            "" + R.drawable.e212, "e212", "1f195"
                    },
                    {
                            "" + R.drawable.e24c, "e24c", "1f51d"
                    },
                    {
                            "" + R.drawable.e213, "e213", "1f199"
                    },
                    {
                            "" + R.drawable.e214, "e214", "1f192"
                    },
                    {
                            "" + R.drawable.e507, "e507", "1f3a6"
                    },
                    {
                            "" + R.drawable.e203, "e203", "1f201"
                    },
                    {
                            "" + R.drawable.e20b, "e20b", "1f4f6"
                    },
                    {
                            "" + R.drawable.e22a, "e22a", "1f235"
                    },
                    {
                            "" + R.drawable.e22b, "e22b", "1f233"
                    },
                    {
                            "" + R.drawable.e226, "e226", "1f250"
                    },
                    {
                            "" + R.drawable.e227, "e227", "1f239"
                    },
                    {
                            "" + R.drawable.e22c, "e22c", "1f22f"
                    },
                    {
                            "" + R.drawable.e22d, "e22d", "1f23a"
                    },
                    {
                            "" + R.drawable.e215, "e215", "1f236"
                    },
                    {
                            "" + R.drawable.e216, "e216", "1f21a"
                    },
                    {
                            "" + R.drawable.e217, "e217", "1f237"
                    },
                    {
                            "" + R.drawable.e218, "e218", "1f238"
                    },
                    {
                            "" + R.drawable.e228, "e228", "1f202"
                    },
                    {
                            "" + R.drawable.e151, "e151", "1f6bb"
                    },
                    {
                            "" + R.drawable.e138, "e138", "1f6b9"
                    },
                    {
                            "" + R.drawable.e139, "e139", "1f6ba"
                    },
                    {
                            "" + R.drawable.e13a, "e13a", "1f6bc"
                    },
                    {
                            "" + R.drawable.e208, "e208", "1f6ad"
                    },
                    {
                            "" + R.drawable.e14f, "e14f", "1f17f"
                    },
                    {
                            "" + R.drawable.e20a, "e20a", "267f"
                    },
                    {
                            "" + R.drawable.e434, "e434", "1f687"
                    },
                    {
                            "" + R.drawable.e309, "e309", "1f6be"
                    },
                    {
                            "" + R.drawable.e315, "e315", "3299"
                    },
                    {
                            "" + R.drawable.e30d, "e30d", "3297"
                    },
                    {
                            "" + R.drawable.e207, "e207", "1f51e"
                    },
                    {
                            "" + R.drawable.e229, "e229", "1f194"
                    },
                    {
                            "" + R.drawable.e206, "e206", "2733"
                    },
                    {
                            "" + R.drawable.e205, "e205", "2734"
                    },
                    {
                            "" + R.drawable.e204, "e204", "1f49f"
                    },
                    {
                            "" + R.drawable.e12e, "e12e", "1f19a"
                    },
                    {
                            "" + R.drawable.e250, "e250", "1f4f3"
                    },
                    {
                            "" + R.drawable.e251, "e251", "1f4f4"
                    },
                    {
                            "" + R.drawable.e14a, "e14a", "1f4b9"
                    },
                    {
                            "" + R.drawable.e149, "e149", "1f4b1"
                    },
                    {
                            "" + R.drawable.e23f, "e23f", "2648"
                    },
                    {
                            "" + R.drawable.e240, "e240", "2649"
                    },
                    {
                            "" + R.drawable.e241, "e241", "264a"
                    },
                    {
                            "" + R.drawable.e242, "e242", "264b"
                    },
                    {
                            "" + R.drawable.e243, "e243", "264c"
                    },
                    {
                            "" + R.drawable.e244, "e244", "264d"
                    },
                    {
                            "" + R.drawable.e245, "e245", "264e"
                    },
                    {
                            "" + R.drawable.e246, "e246", "264f"
                    },
                    {
                            "" + R.drawable.e247, "e247", "2650"
                    },
                    {
                            "" + R.drawable.e248, "e248", "2651"
                    },
                    {
                            "" + R.drawable.e249, "e249", "2652"
                    },
                    {
                            "" + R.drawable.e24a, "e24a", "2653"
                    },
                    {
                            "" + R.drawable.e24b, "e24b", "26ce"
                    },
                    {
                            "" + R.drawable.e23e, "e23e", "1f52f"
                    },
                    {
                            "" + R.drawable.e532, "e532", "1f170"
                    },
                    {
                            "" + R.drawable.e533, "e533", "1f171"
                    },
                    {
                            "" + R.drawable.e534, "e534", "1f18e"
                    },
                    {
                            "" + R.drawable.e535, "e535", "1f17e"
                    },
                    {
                            "" + R.drawable.e219, "e219", "1f534"
                    },
                    {
                            "" + R.drawable.e21a, "e21a", "1f532"
                    },
                    {
                            "" + R.drawable.e21b, "e21b", "1f533"
                    },
                    {
                            R.drawable.e02f + "", "e02f", "1f55b"
                    },
                    {
                            R.drawable.e024 + "", "e024", "1f550"
                    },
                    {
                            R.drawable.e025 + "", "e025", "1f551"
                    },
                    {
                            R.drawable.e026 + "", "e026", "1f552"
                    },
                    {
                            R.drawable.e027 + "", "e027", "1f553"
                    },
                    {
                            R.drawable.e028 + "", "e028", "1f554"
                    },
                    {
                            R.drawable.e029 + "", "e029", "1f555"
                    },
                    {
                            R.drawable.e02a + "", "e02a", "1f556"
                    },
                    {
                            R.drawable.e02b + "", "e02b", "1f557"
                    },
                    {
                            R.drawable.e02c + "", "e02c", "1f558"
                    },
                    {
                            R.drawable.e02d + "", "e02d", "1f559"
                    },
                    {
                            R.drawable.e02e + "", "e02e", "1f55a"
                    },
                    {
                            "" + R.drawable.e332, "e332", "2b55"
                    },
                    {
                            "" + R.drawable.e333, "e333", "274c"
                    },
                    {
                            "" + R.drawable.e24e, "e24e", "00a9"
                    },
                    {
                            "" + R.drawable.e24f, "e24f", "00ae"
                    },
                    {
                            "" + R.drawable.e537, "e537", "2122"
                    }

            }
    };

    private String smile[][];

    private Context mContext;

    /**
     * 表情列表中的起始索引
     */
    private int index;

    /**
     * 表情数
     */
    private int length;

    /**
     * 普通表情
     */
    public final static byte TYPE_NORMAL = 0;

    /**
     * EMOJI表情
     */
    public final static byte TYPE_EjMOJI = 1;

    /**
     * 表情类型
     */
    private byte smileType;

    public static final byte SMILE_COUNT_PER_PAGE = 25;

    public SmileyAdapter(Context context) {
        super();
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return length;
    }

    @Override
    public Object getItem(int position) {
        return smile[getIndex() + position];
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public byte getSmileType() {
        return smileType;
    }

    public void setSmileType(byte smileType) {
        this.smileType = smileType;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setSmile(String[][] smile) {
        this.smile = smile;
    }

    public String[][] getSmile() {
        return smile;
    }

    public String getItemValue(int position) {
        if (position >= 0 && getIndex() +
                position < smile.length) {
            if (getSmileType() == TYPE_NORMAL) {
                return "[" + smile[getIndex() + position][1] + "]";
            } else if (getSmileType() == TYPE_EjMOJI) {
                return TextViewUtil
                        .unicodeToutf8(Integer.parseInt(smile[getIndex() + position][1],
                                16));
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView image;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.smiley_item, null);
            image = (ImageView)convertView.findViewById(R.id.smiley_image);

            convertView.setTag(image);
        } else {
            image = (ImageView)convertView.getTag();
        }
        image.setImageResource(Integer.parseInt(smile[getIndex() +
                position][0]));
        // image.setImageResource(Integer.parseInt(FilterUbbs[position][0]));
        return convertView;
    }

    public abstract static interface OnSmileyListener {
        void onSelect(String smiley);
    }
}

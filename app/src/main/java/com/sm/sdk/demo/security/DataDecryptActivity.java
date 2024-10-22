package com.sm.sdk.demo.security;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.sm.sdk.demo.BaseAppCompatActivity;
import com.sm.sdk.demo.MyApplication;
import com.sm.sdk.demo.R;
import com.sm.sdk.demo.utils.ByteUtil;
import com.sm.sdk.demo.utils.LogUtil;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;
import com.sunmi.pay.hardware.aidlv2.security.SecurityOptV2;

public class DataDecryptActivity extends BaseAppCompatActivity {

    private EditText mEditData;
    private EditText mEditInitIV;
    private EditText mEditKeyIndex;

    private TextView mTvInfo;

    private int mDecryptType = AidlConstantsV2.Security.DATA_MODE_ECB;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_data_decrypt);
        initToolbarBringBack(R.string.security_data_decrypt);
        initView();
    }

    private void initView() {
        RadioGroup keyTypeRadioGroup = findViewById(R.id.decrypt_type_group);
        keyTypeRadioGroup.setOnCheckedChangeListener(
                (group, checkedId) -> {
                    switch (checkedId) {
                        case R.id.rb_decrypt_type1:
                            mDecryptType = AidlConstantsV2.Security.DATA_MODE_ECB;
                            break;
                        case R.id.rb_decrypt_type2:
                            mDecryptType = AidlConstantsV2.Security.DATA_MODE_CBC;
                            break;
                        case R.id.rb_decrypt_type3:
                            mDecryptType = AidlConstantsV2.Security.DATA_MODE_OFB;
                            break;
                        case R.id.rb_decrypt_type4:
                            mDecryptType = AidlConstantsV2.Security.DATA_MODE_CFB;
                            break;
                    }
                }
        );

        mEditData = findViewById(R.id.source_data);
        mEditKeyIndex = findViewById(R.id.key_index);
        mEditInitIV = findViewById(R.id.initialization_vector);

        mTvInfo = findViewById(R.id.tv_info);

        findViewById(R.id.mb_ok).setOnClickListener(this);
//        mEditData.setText("29700D4F0D9ED4F6E2E6DF61D5C309A59DAACBCC22D643465FC6C4F83673CB8F664E567080C38CF41934ABD15C141D205A7CA7DB5CF0C44D6F35E04A7FCBB083F770F70762D25C9657067256DF01F32FD20BDC812AD337664BB159268431D358241799DA3A624512E0891B2E46DD82E2EE0EDA50614706CA2993FF1AAEF3C47AD66AC5327AD5C602C86BC0F4112025AD02811259B4867F247EB47B7A19135C3901E0760603B962DF4B085CCE1FEF2E1C3E75CA3AC6E077A022FE7F7879931FF7EFA2E3F4B8400B90B9436D4C7604DDFFE68709630D23B4D60F54F38F74E0F558D5DDF216EB0275F759D06D8A0239773C27485006DD8DCE75EE9449AD812F2C40D0A800B99E9E885FC3B3D2A8868F5C6749BBA044314C33FB27764D0C7C0397BF70A0A5527A4C269BB94D220A29579F4B192E6EA91883511023740B00AFE7268CF52533362E610111B356F00BA7A2EDE9001735816B653109E526998067BCA78DD42917108C3471FE1846E83E2BBE76C3D6EC1975AD0F7D7AF5E04DF8A6D670013596E763BEA7C3CEBA79438DE9FAC2D4418EAEAB567A56EF6F59BEC39F576196CC40B98427E758FC7AC81466B35D165EDDEB548BC4CCCC35DD59E1A0229CF655A388CDBA578D41DDA5D100655317599E36C0E811396C43085A97079CE13A4F452ABD9D6FF2769876AA0BFB46837BC68056AA829B83967F733EEDEA9070FB4400BF9555E7814C63891F0455622FEC5D89936FAC64C636D365CB9E743ADB568D584962475AAE2FDF35A4840DDAC5491B99388C46530BFEA182DBB5A38846483C3DC5A406F42E9A2D5F07FB6DCEF808DB508F750945FD51DEE43329813CEE05898CB9E6EA9DFD1A1DCE75CB1C53A3A229313D091A28BFF0CFE109591B5CAF16B7B2024D25F4E1949D58FC3F0A66B6FA04C3D0E66CBC89CF18AC01BA239818DC93B9511B7D6A54F4DF2341B30ADCDD318A64AECCE2134D30F4BD7C22209159A62DBEC20E02AD1CD633CA752F3FAFEC18E6EE8E07CE86DA0F7300B26D28D499CF2CF40B7A8BCF543F86E7A164B1FBA52D9897E1F3EDA2A331096B38B7046A7BA777137AE72A728BE61B0FC07A32A5AE265CAF69540323CED1796F0C83DC3CE385ED2D319E20455885815D6DDE57683A5278D0B114DB3767175733FE5440F55E4CFF3FF70564361A7485CF8A42B21512D8D4D070F2BD8D4C407F46A64A9F8ED6B80023B3E777D2255B6B5F7C7493B42349912A667F6762B1D913C8382B556098FDF4177D83E800BC4C004F212E845E7E83581D8CD99DF86F1FE55B95AD695500F530683B11D8233293E3B6ECD2B6998D081C386006FD798CB3ECB405DE5A1DFD0FCD4D2CC620CDC2E7F788BC4A1B621FC0DEDD732EA73E2A3D0DB811C812E7816CF720D5861FAACAD60D9000777AB964E2CF71963A2D864DC1484752B8A46BDD2E804B6689A02D0AFCC89BF4BAC325A55AFDB9846D166AE94A10E813F88805B5D1555E245D0457D2EC690AD1EB522213E71C78F727B784E4C155DBAAAA51197AF1BAF54F89269516FBC91BA8F8D151F4B8C90091AAC29F19CC6321BE1B2B851057D6ACE1D440265514B6AAB65567647F9445A40719923737D19108D34D2EF7F8500F05ED64AA68EE28EC620866197CB0CAC33140A165B7C07F900406317C5E0B5E6D55FFE0EF36B08122405004C35FB4B283A7DE7A2292443C2508CFC5D2241CA0EB6632405ED7D7A913F534BE04CE69542618DF8A00B624CA9EB0F68D6EB62502E2703C0F6B823590DDF4FA2B0F293E5C69CEBFCB26ADA72BF4675D47AA149900133C789F72C8D25A701652BCD47D332D56748C151D3B847309CC77671780419FFE7364AC1E900D76C74FB0F0C0E31F913980C6244071F5B9655200ACD07EA7BEC524B57CF2F43AC1BA6813D0168E3CCDF8A766A9EBE865FB70EE8D1921D26C66329D3ACE1EFBB5AFB80D64A73422EC61415869D58BACE4D947EA54FCF6EE39D4D1CFB1AC23CBE4AFFC102773B5CA5E91180D2C24648853F2EBE208C71F2D43E6727B3FF08F373A387A0263D7CF67B77334B9ECCDC64498531154C3E79B6809835074573B35A18F5DE9DC109D6092C5B09C1F1FEDC7044CA95D2589DF960601B8FF252E00D18C94EDE986B22C27B89189D8F30350C1F1DD22DF97EB198F078CFBF1F098A606FAEDB11C866B72D0A39049F335E326CA2A0946C764EAD7B2AFBC354F65E1B338B0B886BF82328283C94BB527C5D293260914F774D81AA31789EE8402FAD594E53753FACE1C42D653A493193481C35A880B67DEBD888ECCDB517212DFA4C043B2F0B6A6822FB9B1AC9912692292DA136F935BDF6BCEC6DD66D64E8CB1925BCFEC9EC5F9311E275E90EF45CCCE36AA496786ACE0052FB0C737A84755702ABA89C19A7DF4E93828A5EE6AA6A832A3FF1C6FAEC22E556484C43EF7510B39F5251DBF55F83CCB04A141E854D36F1A07B356399E47B8254282428769932801500564BABF6CE735E06BDA76EA6E684A5E58FAE174DAEBCC776F81B21A6DD3C1048B39C910BABF1C87B914980221EC4724FEAF165E592380A28F151DBBEDEB4F33C222D00094917FA03A35BE218583A81A8846AE9FFA16F233201A362400CEF2B84090C6C235F06361B32794E5193A04AA37D8D1222B806FEAA2A14BD0C500085C4D1D3E44986B7988CA79D3ECCFC33BF0DA6CF3DF325C180FB0500067B3BAA6FF03496D5CDF8FCF83400EABB6AF452A4E7D3321C82A40F1CC02E892FE74BA3844EF4CB8B81125CFEC7D0104583EE922422D777495B1CC8CFB98BD9D954D209D5CD709619081A6186DE0FBBF1B1A2DED8A15163B7D662F9BF16437D3AA36E360CFAB967EB0436A4F0BB265858B5560044573A8C14656D675ED30B2B86B4AC6DCBEA4DE93F36CDDB27697784292A051B81B514132F870F4754EB2434926AB3E9F886AFD1A143B08CCE6EA410A143B93E07D5881A0B67CF0D8B1AF9F8D8AE4D8B51E65EF35920AF7120751304DDC06C2D2E23C66A034BF658F0367206A6457971A10F75E7B09D599963083204B644060D927AF6F4C3C57E3282830B85589A7BE918E6CBED2FC93DB8FECD02673EFE03F4774931F38BE381BB596D1FC9B972DCD7D08C9E6DB6F588E264851A84C1EB151EF772DC1A04429BB306C728FED773F5BFDFB6CF28CBB89F0E152FED1C97D756A1454B8FCD8553199A3B60E85D184ADE9DD80008082D73A72FE6D1C381E96C8FB017CAB0A5D2526661959E15573C838DF71FCBDE09EB8D1156105D1E87F87BD3D8524B9953159DC56D58552B452FBC682C099A9606F4B39EDEF8B85BF2CC1CA00C8A15BD3AA3CC7E81ADF47DE8EEF7F50FAD0116B5703A39888444ABFB29D4B730C4268412593995C9481AA7DBE1F780AC5F21DEC24696A64B570C762BA70BF5474D13B5F4148FD17B02B32043084B1C1CB2CC4C4A8D8459D4214904ADEABAB9FBE4B0BDF8FC476A980811C43255827C64B603ABB9843B4F54E2B7B9051277AD0F838E44AF52C091B4808F4DF13F699AE912739AE70BA479358F88024C05B15C79DD69270C41C0846CB17A66E9D13133C44CEE759A099544E0163CEF2B5A94446FCE1573D032F0DF7D545D0363445CB132FFF64186269C0B84246EA27F7C1987A31300E506ABFACB309D421080FDF54B82CE6D5E741EE6FB2CC874FAC9EC27750E9C8C07D162E89E1BBE347284BD659EAB52134B7F5AAC7805824D9ABDF82F71C9FCF7EDBE269EA8D43E0589078F9CF9ECA2CB57840DFFCBF7E6FA801DDD6D9AE332902092EA79239610037685295A0363E42FC01C98AEDF7BB274F29805839FA935925F4651B96F9FEFFBB673CA9567431B330AB3C922451FAF88F49AE7C7F2FBFF495D39C28FC4F339A2F4FD62184963BE92984C323E4565513FD5B015C2A692AE28B4BA482167F60768D01D0F64BC0E009258FDF68102BAD24D729E8711DEBB5A81C0907F46857F2FDC56636BA0669D10E3ED1CA9F6727EC82C526A1E7550BFC502CB5EC01F1733F84BA7C1320A146E98423B0D1A5B09B2FB5CCEDDB5EA428C40EE26D45412F7521B9C4AE20E1BBC5795E97FF9D0CAA07E8BDA24B5A43A158BA682583058156DAF707D32E14415A58C2FABAF4B59B73D3534D085ABF95968CB43508EDEAAC3A8E9C538765AB9F8F682BE0E0CEC01B8F35DC78EB5A8DFC721AEFD2ABA14EBD6918E54AF5AC97B3F1BFC0E99849D717DB30F094F6F43BFE44B95B01EC261340F42559963C9967B2D38FA0AB718D34296D7C86629674220B723B7F1120E092F0AFAE32E15CD9EFE2E3D2F3F8C643BD5A3749B01CF2BAFD00D1873AC0B1AE9AAF0B4CC156A55970DEBE8F967E8B2AFE8F5C47BF866741C9294E460E86972F90B0108475CD3FFC9214495EF0005CB2ED2D4AFA2389AE7A916B89CEC2067D45B061D3FE46A6BE9ADD4AE53FCFBF0D9561ABA6987DF746277AD8A73CA582FA3B066685B8B894AC700AD57B75A3AE850EAE62DF26C6C03548CAB9190C49B54028491A8644110BF06B51E1931A1B6C8D2F0A792B72E4F65504897A49E3B7B7572F7863F45A6FB045A5F7465B4D07D409F154B833E78E9894758FF36635EF90F9C93CFB2192B628BC5C132EB739088DFE3282639EC6FF2A1434E3F18FBF2CF5C93B9BB664BA50E223AADF01124BC8061AF77B1CC0A54A1C3CEBFD4AF67DE0D63C789F1B5081D8168674622860CFED89E377CF407B92DB3B8765C2B5673AE67D2F3B95621CCBF48AB32631DD2AB8171874DF666ABC77E494BED988BAB4EAE6C14BFBFB83E8411F9783C0D0D15FFB1866B5DEC855D6642903A3AFD0363BF14E1BC3A9E4611C698A5D9F08AD29F40570E0BC16EA80DB8EA337F7AA6C95895A2EFD44BE0EB0918DA4A6DA325518B910571DC2E0CD9EDD7F05DE6BA516D25C8DBAA9BB497BD7E20CBBBCB8F361E34BC224D979103C10683D51F8E2DD729D0EFB1C0064E61F506236C72A668B859CD16E8468CA51492F7CA59355C6E912BCD242AD1D888E57955641CA2CDAF8FF0859C019075DB10DD87E905429599B4EC5317F0AE4685FC9602F60126F406D962FDBF4EFEEBDF95F47CE1A0AF3BFAF445176E747FA294F8A5B65F906E6ACBA69B4D21B99603422C6DF3408E78FB245A042A7C58CF102E3B95B12A6CDA5B6133811A3DB8CE8F3469424B319760D57BF1739DC1BF0C0F64F66F51127148AFD86080D0A9703703536EA229F2E58AB4256A5D065A0344B592CA528A4E68836FFC631B7E9348A0EC4D3ED93B09C916DFA64D77BA8F995C1FAD75A728F014931BA88084439D688F2103E727AC2808AB0035ECC00BA19033E12C47EB609BC85824CA8E616BCC092D8FCA36B686F1E169B66618BE4F570771C");
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.mb_ok:
                dataDecrypt();
                break;
        }
    }

    private void dataDecrypt() {
        try {
            SecurityOptV2 securityOptV2 = MyApplication.app.securityOptV2;

            String ivStr = mEditInitIV.getText().toString();
            String dataStr = mEditData.getText().toString();
            String keyIndexStr = mEditKeyIndex.getText().toString();

            int keyIndex;
            try {
                keyIndex = Integer.parseInt(keyIndexStr);
                if (keyIndex < 0 || keyIndex > 199) {
                    showToast(R.string.security_mksk_key_index_hint);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                showToast(R.string.security_mksk_key_index_hint);
                return;
            }
            if (mDecryptType != AidlConstantsV2.Security.DATA_MODE_ECB && ivStr.length() != 16) {
                showToast(R.string.security_init_vector_hint);
                return;
            }
            if (dataStr.trim().length() == 0) {
                showToast(R.string.security_source_data_hint);
                return;
            }
            if ((mDecryptType != AidlConstantsV2.Security.DATA_MODE_OFB &&
                    mDecryptType != AidlConstantsV2.Security.DATA_MODE_CFB) &&
                    dataStr.length() % 16 != 0) {
                showToast(R.string.security_source_data_hint);
                return;
            }
            byte[] dataIn = ByteUtil.hexStr2Bytes(dataStr);
            byte[] dataOut = new byte[dataIn.length];
            byte[] ivByte;
            if (mDecryptType != AidlConstantsV2.Security.DATA_MODE_ECB) {
                ivByte = ByteUtil.hexStr2Bytes(ivStr);
            } else {
                ivByte = null;
            }
            addStartTimeWithClear("dataDecrypt()");
            int result = securityOptV2.dataDecrypt(keyIndex, dataIn, mDecryptType, ivByte, dataOut);
            addEndTime("dataDecrypt()");
            if (result == 0) {
                String hexStr = ByteUtil.bytes2HexStr(dataOut);
                LogUtil.e(TAG, "dataDecrypt output:" + hexStr);
                mTvInfo.setText(hexStr);
            } else {
                toastHint(result);
            }
            showSpendTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

package com.minister.component.utils;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.HexUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.BCUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import com.minister.component.utils.constants.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.*;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.params.*;
import org.bouncycastle.crypto.signers.DSAEncoding;
import org.bouncycastle.crypto.signers.PlainDSAEncoding;
import org.bouncycastle.crypto.signers.SM2Signer;
import org.bouncycastle.crypto.signers.StandardDSAEncoding;
import org.bouncycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Sm2算法工具类
 *
 * <p>
 * 16进制字符串密钥说明：<br/>
 * 压缩公钥：长度为66，以02或03开头；<br/>
 * 未压缩公钥：长度为128或130，128位为130位省略了开头04<br/>
 * 公钥拆分：128位公钥可以拆成长度均为64的x和y
 * 私钥：实际类型为BigInteger，长度小于等于64
 * </p>
 *
 * @author QIUCHANGQING620
 * @date 2021-03-18 09:50
 */
@Slf4j
public class Sm2Util {

    public static void main(String[] args) {
        // data和dHex不能同时为空
        String data = "";
        String dHex = "1234567890abcdefABCDEF";
        String priHex = "";
        String pubHex = "";
        String signHex = "";
        String id = "1234567812345678";

        String dataHex = StringUtils.isNotBlank(data) ? HexUtil.encodeHexStr(data) : dHex;
        String idHex = HexUtil.encodeHexStr(id);
        Sm2Util sm2 = new Sm2Util();
        String privateKeyHex = StringUtils.isBlank(priHex) ? sm2.getPrivateKeyHex() : priHex;
        String publicKeyHex = StringUtils.isBlank(pubHex) ? sm2.getPublicKeyHex(true) : pubHex;

        sm2 = new Sm2Util(privateKeyHex, publicKeyHex);

        System.out.println("数据：" + dataHex);
        System.out.println("私钥：" + sm2.getPrivateKeyHex());
        System.out.println("公钥压缩：" + sm2.getPublicKeyHex(true));
        System.out.println("公钥未压缩：" + sm2.getPublicKeyHex(false));
        System.out.println("签名id：" + idHex);

        if (StringUtils.isBlank(signHex)) {
            signHex = sm2.signHex(dataHex, idHex);
            System.out.println("生成的签名：" + signHex);
        }
        System.out.println("签名验证结果：" + sm2.verifyHex(dataHex, signHex, idHex));
    }

    /**
     * 锁
     */
    protected final Lock lock = new ReentrantLock();

    protected SM2Signer signer;
    protected SM2Engine engine;

    private ECPrivateKeyParameters privateKeyParams;
    private ECPublicKeyParameters publicKeyParams;

    private DSAEncoding encoding = PlainDSAEncoding.INSTANCE;
    private Digest digest = new SM3Digest();
    private SM2Engine.Mode mode = SM2Engine.Mode.C1C3C2;

    /**
     * SM2默认曲线
     */
    public static final String SM2_CURVE_NAME = "sm2p256v1";

    /**
     * 构造SM2默认曲线
     */
    public static final X9ECParameters SM2_EC_PARAMETERS = GMNamedCurves.getByName(SM2_CURVE_NAME);
    /**
     * SM2推荐曲线参数（来自https://github.com/ZZMarquis/gmhelper）
     */
    public static final ECDomainParameters SM2_DOMAIN_PARAMS;

    static {
        SM2_DOMAIN_PARAMS = new ECDomainParameters(
                SM2_EC_PARAMETERS.getCurve(),
                SM2_EC_PARAMETERS.getG(),
                SM2_EC_PARAMETERS.getN(),
                SM2_EC_PARAMETERS.getH()
        );
    }

    // -------------------------------------------------------------------------------- Constructor

    public Sm2Util() {
        this((byte[]) null, null);
    }

    public Sm2Util(String privateKeyHex, String publicKeyHex) {
        this(
                StringUtils.isBlank(privateKeyHex) ? null : new BigInteger(privateKeyHex, 16),
                HexUtil.decodeHex(publicKeyHex)
        );
    }

    public Sm2Util(byte[] privateKey, byte[] publicKey) {
        this(privateKey == null ? null : new BigInteger(privateKey), publicKey);
    }

    public Sm2Util(BigInteger privateKey, byte[] publicKey) {
        this(
                privateKey == null ? null : new ECPrivateKeyParameters(privateKey, SM2_DOMAIN_PARAMS),
                ArrayUtils.isEmpty(publicKey) ? null : new ECPublicKeyParameters(SM2_DOMAIN_PARAMS.getCurve().decodePoint(publicKey), SM2_DOMAIN_PARAMS)
        );
    }

    /**
     * 构造 <br>
     * 私钥和公钥同时为空时生成一对新的私钥和公钥<br>
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param privateKeyHex      私钥16进制
     * @param publicKeyPointXHex 公钥X16进制
     * @param publicKeyPointYHex 公钥Y16进制
     */
    public Sm2Util(String privateKeyHex, String publicKeyPointXHex, String publicKeyPointYHex) {
        this(BCUtil.toSm2Params(privateKeyHex), BCUtil.toSm2Params(publicKeyPointXHex, publicKeyPointYHex));
    }

    /**
     * 构造 <br>
     * 私钥和公钥同时为空时生成一对新的私钥和公钥<br>
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param privateKey      私钥
     * @param publicKeyPointX 公钥X（16进制字符串decode）
     * @param publicKeyPointY 公钥Y（16进制字符串decode）
     */
    public Sm2Util(byte[] privateKey, byte[] publicKeyPointX, byte[] publicKeyPointY) {
        this(BCUtil.toSm2Params(privateKey), BCUtil.toSm2Params(publicKeyPointX, publicKeyPointY));
    }

    public Sm2Util(ECPrivateKeyParameters privateKeyParams, ECPublicKeyParameters publicKeyParams) {
        this.privateKeyParams = privateKeyParams;
        this.publicKeyParams = publicKeyParams;
        this.init();
    }


    // -------------------------------------------------------------------------------- Static method

    /**
     * 转换为ECPublicKeyParameters
     *
     * @param xHex 公钥X
     * @param yHex 公钥Y
     * @return ECPublicKeyParameters
     */
    public static ECPublicKeyParameters toParams(String xHex, String yHex) {
        return BCUtil.toParams(xHex, yHex, SM2_DOMAIN_PARAMS);
    }

    /**
     * 将未压缩公钥压缩成压缩公钥
     *
     * @param publicKeyHex 未压缩公钥
     * @return 压缩公钥
     */
    public static String compressPublicKey(String publicKeyHex) {
        int len = publicKeyHex.length();
        if (len != 66 && len != 128 && len != 130) {
            throw new RuntimeException("can not identify publicKey");
        }
        if (len == 66) {
            return publicKeyHex;
        }
        // 将未压缩公钥加上未压缩标识.
        if (len == 128) {
            publicKeyHex = "04" + publicKeyHex;
        }
        //提取公钥点
        ECPoint pukPoint = SM2_EC_PARAMETERS.getCurve().decodePoint(HexUtil.decodeHex(publicKeyHex));

        return HexUtil.encodeHexStr(pukPoint.getEncoded(Boolean.TRUE));
    }

    /**
     * 将压缩公钥解压为未压缩公钥
     *
     * @param publicKeyHex 压缩公钥
     * @return 130位未压缩公钥
     */
    public static String unCompressPublicKey(String publicKeyHex) {
        int len = publicKeyHex.length();
        if (len != 66 && len != 128 && len != 130) {
            throw new RuntimeException("can not identify publicKey");
        }
        if (len == 128 || len == 130) {
            return publicKeyHex;
        }
        //提取公钥点
        ECPoint pukPoint = SM2_EC_PARAMETERS.getCurve().decodePoint(HexUtil.decodeHex(publicKeyHex));

        return HexUtil.encodeHexStr(pukPoint.getEncoded(Boolean.FALSE));
    }

    /**
     * 获取公钥X
     *
     * @param publicKeyHex 未压缩公钥
     * @return 公钥X
     */
    public static String getPublicKeyPointX(String publicKeyHex) {
        int len = publicKeyHex.length();
        if (len != 128 && len != 130) {
            throw new RuntimeException("can not identify publicKey");
        }
        publicKeyHex = subPublicKey(publicKeyHex);
        return publicKeyHex.substring(0, 64);
    }

    /**
     * 获取公钥Y
     *
     * @param publicKeyHex 未压缩公钥
     * @return 公钥Y
     */
    public static String getPublicKeyPointY(String publicKeyHex) {
        int len = publicKeyHex.length();
        if (len != 128 && len != 130) {
            throw new RuntimeException("can not identify publicKey");
        }
        publicKeyHex = subPublicKey(publicKeyHex);
        return publicKeyHex.substring(64, 128);
    }

    /**
     * 获取128位未压缩公钥
     *
     * @param publicKeyHex 未压缩公钥
     * @return 128位未压缩公钥
     */
    public static String subPublicKey(String publicKeyHex) {
        int len = publicKeyHex.length();
        if (len != 128 && len != 130) {
            throw new RuntimeException("can not identify publicKey");
        }
        return len == 130 ? publicKeyHex.substring(2) : publicKeyHex;
    }

    // -------------------------------------------------------------------------------- Sign and Verify

    /**
     * 用私钥对信息生成数字签名
     *
     * @param dataHex 被签名的数据数据
     * @param idHex   ID的Hex值
     * @return 签名
     */
    public String signHex(String dataHex, String idHex) {
        return HexUtil.encodeHexStr(sign(HexUtil.decodeHex(dataHex), HexUtil.decodeHex(idHex)));
    }

    /**
     * 私钥签名
     *
     * @param data 被签名的数据数据
     * @param id   可以为null，若为null，则默认withId为字节数组:"1234567812345678".getBytes()
     * @return 签名
     */
    public byte[] sign(byte[] data, byte[] id) {
        lock.lock();
        try {
            final SM2Signer signer = getSigner();
            CipherParameters param = new ParametersWithRandom(getCipherParameters(KeyType.PrivateKey));
            if (id != null) {
                param = new ParametersWithID(param, id);
            }
            signer.init(true, param);
            signer.update(data, 0, data.length);
            return signer.generateSignature();
        } catch (CryptoException e) {
            throw new RuntimeException(ExceptionUtil.getMessage(e), e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 用公钥检验数字签名的合法性
     *
     * @param dataHex 数据签名后的数据的Hex值
     * @param signHex 签名的Hex值
     * @param idHex   ID的Hex值
     * @return 是否验证通过
     */
    public boolean verifyHex(String dataHex, String signHex, String idHex) {
        return verify(HexUtil.decodeHex(dataHex), HexUtil.decodeHex(signHex), HexUtil.decodeHex(idHex));
    }

    /**
     * 用公钥检验数字签名的合法性
     *
     * @param data 数据签名后的数据
     * @param sign 签名
     * @param id   可以为null，若为null，则默认withId为字节数组:"1234567812345678".getBytes()
     * @return 是否验证通过
     */
    public boolean verify(byte[] data, byte[] sign, byte[] id) {
        final SM2Signer signer = getSigner();
        lock.lock();
        try {
            CipherParameters param = getCipherParameters(KeyType.PublicKey);
            if (id != null) {
                param = new ParametersWithID(param, id);
            }
            signer.init(false, param);
            signer.update(data, 0, data.length);
            return signer.verifySignature(sign);
        } finally {
            lock.unlock();
        }
    }

    // -------------------------------------------------------------------------------- Encrypt

    /**
     * 加密，SM2非对称加密的结果由C1,C2,C3三部分组成，其中：
     *
     * <pre>
     * C1 生成随机数的计算出的椭圆曲线点
     * C2 密文数据
     * C3 SM3的摘要值
     * </pre>
     *
     * @param data 被加密的bytes
     * @return 加密后的bytes
     */
    public byte[] encrypt(byte[] data) {
        final SM2Engine engine = getEngine();
        lock.lock();
        try {
            engine.init(true, new ParametersWithRandom(getCipherParameters(KeyType.PublicKey)));
            return engine.processBlock(data, 0, data.length);
        } catch (InvalidCipherTextException e) {
            throw new RuntimeException(ExceptionUtil.getMessage(e), e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * SM2加密算法
     *
     * @param publicKey     公钥
     * @param data          明文数据
     * @return
     */
//    public static String encrypt(PublicKey publicKey, String data) {
//
//        ECPublicKeyParameters ecPublicKeyParameters = null;
//        if (publicKey instanceof BCECPublicKey) {
//            BCECPublicKey bcecPublicKey = (BCECPublicKey) publicKey;
//            ECParameterSpec ecParameterSpec = bcecPublicKey.getParameters();
//            ECDomainParameters ecDomainParameters = new ECDomainParameters(ecParameterSpec.getCurve(),
//                    ecParameterSpec.getG(), ecParameterSpec.getN());
//            ecPublicKeyParameters = new ECPublicKeyParameters(bcecPublicKey.getQ(), ecDomainParameters);
//        }
//
//        SM2Engine sm2Engine = new SM2Engine();
//        sm2Engine.init(true, new ParametersWithRandom(ecPublicKeyParameters, new SecureRandom()));
//
//        byte[] arrayOfBytes = null;
//        try {
//            byte[] in = data.getBytes("utf-8");
//            arrayOfBytes = sm2Engine.processBlock(in,0, in.length);
//        } catch (Exception e) {
//            log.error("SM2加密时出现异常:", e);
//        }
//        return Hex.toHexString(arrayOfBytes);
//    }

    // -------------------------------------------------------------------------------- Decrypt

    /**
     * 解密
     *
     * @param data SM2密文，实际包含三部分：ECC公钥、真正的密文、公钥和原文的SM3-HASH值
     * @return 加密后的bytes
     */
    public byte[] decrypt(byte[] data) {
        final SM2Engine engine = getEngine();
        lock.lock();
        try {
            engine.init(false, getCipherParameters(KeyType.PrivateKey));
            return engine.processBlock(data, 0, data.length);
        } catch (InvalidCipherTextException e) {
            throw new RuntimeException(ExceptionUtil.getMessage(e), e);
        } finally {
            lock.unlock();
        }
    }

    /**
     * SM2解密算法
     *
     * @param privateKey        私钥
     * @param cipherData        密文数据
     * @return
     */
//    public static String decrypt(PrivateKey privateKey, String cipherData) {
//        byte[] cipherDataByte = Hex.decode(cipherData);
//
//        BCECPrivateKey bcecPrivateKey = (BCECPrivateKey) privateKey;
//        ECParameterSpec ecParameterSpec = bcecPrivateKey.getParameters();
//
//        ECDomainParameters ecDomainParameters = new ECDomainParameters(ecParameterSpec.getCurve(),
//                ecParameterSpec.getG(), ecParameterSpec.getN());
//
//        ECPrivateKeyParameters ecPrivateKeyParameters = new ECPrivateKeyParameters(bcecPrivateKey.getD(),
//                ecDomainParameters);
//
//        SM2Engine sm2Engine = new SM2Engine();
//        sm2Engine.init(false, ecPrivateKeyParameters);
//
//        String result = null;
//        try {
//            byte[] arrayOfBytes = sm2Engine.processBlock(cipherDataByte, 0, cipherDataByte.length);
//            return new String(arrayOfBytes, "utf-8");
//        } catch (Exception e) {
//            log.error("SM2解密时出现异常:", e);
//        }
//        return result;
//    }

    // -------------------------------------------------------------------------------- Get parameter

    /**
     * 只获取私钥里的d，32字节
     *
     * @return 压缩得到的X
     */
    public byte[] getEncodePrivateKey() {
        return this.privateKeyParams.getD().toByteArray();
    }

    /**
     * 获取16进制长度为64的私钥字符串
     *
     * @return 压缩得到的X（输出长度为64，位数不够前面补0）
     */
    public String getPrivateKeyHex() {
        String privateKeyHex = this.privateKeyParams.getD().toString(16);

        return StrUtil.padPre(privateKeyHex, 64, Constants.STR_ZERO);
    }

    /**
     * 获取编码后公钥<br>
     * 公钥前面的02或者03表示是压缩公钥,04表示未压缩公钥,04的时候,可以去掉前面的04<br>
     * 未压缩的公钥转为16进制后，0-64为xhex，64-128为yhex
     *
     * @param compressed 是否压缩
     * @return 压缩得到的X
     */
    public byte[] getEncodePublicKey(boolean compressed) {
        return this.publicKeyParams.getQ().getEncoded(compressed);
    }

    /**
     * 获取16进制长度为66/130的公钥
     *
     * @param compressed 是否压缩
     * @return 压缩公钥长度66，未压缩公钥长度130
     */
    public String getPublicKeyHex(boolean compressed) {
        return HexUtil.encodeHexStr(getEncodePublicKey(compressed));
    }

    // -------------------------------------------------------------------------------- Set parameter

    /**
     * 设置DSA signatures的编码为StandardDSAEncoding
     *
     * @return this
     */
    public Sm2Util useStandardEncoding() {
        return setEncoding(StandardDSAEncoding.INSTANCE);
    }

    /**
     * 切换SM2模式为C1C2C3
     *
     * @return this
     */
    public Sm2Util useC1C2C3Mode() {
        return setMode(SM2Engine.Mode.C1C2C3);
    }

    /**
     * 设置私钥参数
     *
     * @param privateKeyParams 私钥参数
     * @return this
     */
    public Sm2Util setPrivateKeyParams(ECPrivateKeyParameters privateKeyParams) {
        this.privateKeyParams = privateKeyParams;
        return this;
    }

    /**
     * 设置公钥参数
     *
     * @param publicKeyParams 公钥参数
     * @return this
     */
    public Sm2Util setPublicKeyParams(ECPublicKeyParameters publicKeyParams) {
        this.publicKeyParams = publicKeyParams;
        return this;
    }

    /**
     * 设置DSA signatures的编码
     *
     * @param encoding {@link DSAEncoding}实现
     * @return this
     */
    public Sm2Util setEncoding(DSAEncoding encoding) {
        this.encoding = encoding;
        this.signer = null;
        return this;
    }

    /**
     * 设置Hash算法
     *
     * @param digest {@link Digest}实现
     * @return this
     */
    public Sm2Util setDigest(Digest digest) {
        this.digest = digest;
        this.engine = null;
        this.signer = null;
        return this;
    }

    /**
     * 设置SM2模式，旧版是C1C2C3，新版本是C1C3C2
     *
     * @param mode {@link SM2Engine.Mode}
     * @return this
     */
    public Sm2Util setMode(SM2Engine.Mode mode) {
        this.mode = mode;
        this.engine = null;
        return this;
    }

    // -------------------------------------------------------------------------------- Private method

    /**
     * 初始化<br>
     * 私钥和公钥同时为空时生成一对新的私钥和公钥<br>
     * 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密（签名）或者解密（校验）
     */
    private void init() {
        if (null == this.privateKeyParams && null == this.publicKeyParams) {
            AsymmetricCipherKeyPair asymmetricCipherKeyPair = generateKeyPair();
            this.privateKeyParams = (ECPrivateKeyParameters) asymmetricCipherKeyPair.getPrivate();
            this.publicKeyParams = (ECPublicKeyParameters) asymmetricCipherKeyPair.getPublic();
        }
    }

    /**
     * 生成公钥和私钥
     *
     * @return 公钥和私钥
     */
    private AsymmetricCipherKeyPair generateKeyPair() {
        //1.创建密钥生成器
        ECKeyPairGenerator keyPairGenerator = new ECKeyPairGenerator();

        //2.初始化生成器,带上随机数
        keyPairGenerator.init(new ECKeyGenerationParameters(SM2_DOMAIN_PARAMS, RandomUtil.getSecureRandom()));

        //3.生成密钥对
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * 获取密钥类型对应的加密参数对象{@link CipherParameters}
     *
     * @param keyType Key类型枚举，包括私钥或公钥
     * @return {@link CipherParameters}
     */
    private CipherParameters getCipherParameters(KeyType keyType) {
        switch (keyType) {
            case PublicKey:
                Assert.notNull(this.publicKeyParams, "PublicKey must be not null !");
                return this.publicKeyParams;
            case PrivateKey:
                Assert.notNull(this.privateKeyParams, "PrivateKey must be not null !");
                return this.privateKeyParams;
            default:
                return null;
        }
    }

    /**
     * 获取{@link SM2Signer}，此对象为懒加载模式
     *
     * @return {@link SM2Signer}
     */
    private SM2Signer getSigner() {
        if (null == this.signer) {
            this.signer = new SM2Signer(this.encoding, this.digest);
        }
        return this.signer;
    }

    /**
     * 获取{@link SM2Engine}，此对象为懒加载模式
     *
     * @return {@link SM2Engine}
     */
    private SM2Engine getEngine() {
        if (null == this.engine) {
            this.engine = new SM2Engine(this.digest, this.mode);
        }
        return this.engine;
    }

}

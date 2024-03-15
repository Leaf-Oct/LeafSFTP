## 简介
基于Apache MINA SSHD库开发的Android平台的SFTP服务端，可供与其它设备(如电脑)安全的双向文件传输。

其它传输方式例如FTP, HTTP，存在明文传输，不安全，单向传输等问题，故采用SFTP作为解决方案。

在UI设计上，~~抄袭~~参考了多年前的开源项目droid-sftp(hakkon.sftpserver)

## 未来规划

安卓系统软件生态对sftp支持不足，因此未来将加入FTP, WebDAV等传输协议的支持。

同时加入简单文本传输的功能，类似即时通讯

### 开源地址

https://github.com/Leaf-Oct/LeafSFTP

### 使用的开源库清单

- Apache MINA SSHD
- Apache commons-io
- EventBus
- Room

#### 开发者

十月叶

#### 图标画师

心脏弱真君
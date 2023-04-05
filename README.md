# gulimall

#### 介绍
谷粒商城

#### 软件架构
软件架构说明
##### springboot+springcloud+springcloudalibaba

####启动教程

1. 配置本地HOST,使用ping gulimall.com检查是否指向192.168.56.10
2. 修改订单配置文件微信回调地址,配置文件nacos->gulimall-order.yml,修改为natapp的域名地址,natapp重启后域名会改.
3. 修改虚拟机网关ip地址, vi /mydata/nginx/conf/nginx.conf,将ip修改为本地电脑ip,重启nginx容器,docker restart nginx
4. 将jquery-qrcode.min.js放置在nginx的目录/mydata/nginx/html/static/order/confirm/js/目录下.
5. layui.zip放在static目录下并解压
#### 使用说明

1.  xxxx
2.  xxxx
3.  xxxx

#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request


#### 码云特技


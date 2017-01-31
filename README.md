#LYLab

Release under LGPL license, consult your rights before using.

## Explanation

This project contains follow project:

1. https://github.com/liyang-bsy/MongoDBService
2. https://github.com/liyang-bsy/LYTaskQueue
3. https://github.com/liyang-bsy/LYPlan

This project contains even more powerful utilities:

* net.vicp.lylab.core.model:

Contains different utility model, like ReverseMap, Pair, InetAddr, Message etc.

* net.vicp.lylab.core.pool:

Contains different kind of pools, can be use directly. Specifically, AutoGeneratePool have creator/validate option:
>1. net.vicp.lylab.utils.creator
>2. net.vicp.lylab.utils.operation

* net.vicp.lylab.message.Mail:

Contains a simple mail client, at least it could send email through Gmail (I tested).

* net.vicp.lylab.mybatis:

Contains a Config-Mybatis connector, and Multi Data Source Session Manager.

* net.vicp.lylab.server:

A simple but useful server logic, including dispatcher logic, filter, and server runtime.

* net.vicp.lylab.utils.atomic:

Contains a synchronized based atomic classes, can be more safe and reliable than java.util.concurrent.atomic.

* net.vicp.lylab.utils.cache:

A key-rule(default is MD5) based grid-distribute cache system.

* net.vicp.lylab.utils.client:

Contains simple long socket embedded client, works with RDMA protocol or RpcSlb protocol.

* net.vicp.lylab.utils.internet:

Contains a serials of powerful utilities to implement sync/async socket. and easily be apply to server/client. In this section, I also provide protocol define, to encode and decode net stream. To enhance performance of asyn socket (Async session), Async Transfer with Pooled Handler to receive data from client is recommended.

* net.vicp.lylab.utils.permanent:

A serials of simple utilities to save data into disks with different structure.

* net.vicp.lylab.utils.Config:

A powerful configuration set for any system requires complex configurations.

* net.vicp.lylab.utils.Caster:

A small but useful utility to convert from basic types. In addition, it also provide Array, Set, List convertor.

* net.vicp.lylab.utils.Utils, net.vicp.lylab.utils.Algorithm, DNS, Command:

Utils.java contains a bundle of utilities method I may use in daily developing. Algorithm.java contains KMP search, safe quick sort, md5 and other hash algorithms. DNS.java contains native DNS resolve method. Command.java is used to execute local shell command.

## Reading Finished? Freak out!

Thanks for reading this document and using my source!


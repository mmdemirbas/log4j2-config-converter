package com.mmdemirbas.log4j2.configconverter

import java.io.Reader
import java.io.Writer

object SnakeYaml : Format() {
    // todo: unwrapIfSingle özelliğinin çalıştığından emin olmak için test yazılabilir. Benzer şekilde farklı feature'lar için testler yazılmalı

    // todo: unwrapIfSingle özelliği, generate edilen map'lerde kullanılmalı mı? Kullanılacaksa mümkün olan her yerde mi kullanılsa? Okuma kısmı nasıl olacak?

    // todo: extra'lar genelde key-value pair'ler olduğu için composite entry'lerden önce dump edilebilir. Ya da simple olan kısımları önce, composite olanlar sonra yapılabilir.

    // todo: equalsIgnoreCase kullanılmalı

    // todo: parsing işlemi olabildiğince toleranslı yapılsın. Parse edilemeyen kısımla ilgili warning verilsin ama işlem iptal edilmesin.

    override fun read(reader: Reader) =
            (org.yaml.snakeyaml.Yaml().load(reader) as Map<String, Any>).map("Configuration")!!.toConfig()

    override fun write(config: Config, writer: Writer) =
            writer.write(org.yaml.snakeyaml.Yaml().dumpAsMap(config.configToYamlMap()))
}
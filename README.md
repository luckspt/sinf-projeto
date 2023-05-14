# SInf Projeto myCloud

**Grupo**: si003

**Membros**:
- 56894 - Pedro Alves
- 56897 - Pedro Almeida
- 56926 - Lucas Pinto

## Compilar
Para compilar o projeto, basta executar o _shell script_ `compile.sh` na raiz do projeto.

Este _shell script_ compila o projeto com `javac` para a diretoria `target/classes`. Esses ficheiros são _java bytecode_.

## Executar
Para executar o projeto, basta executar o _shell script_ referente ao componente que se pretende executar.

Os argumentos referentes são passsados na totalidade para o componente, não sendo alterados pelo _shell script_.

O _shell script_ executa o componente com `java`.

### Servidor
Para executar o servidor, basta executar o _shell script_ `myCloudServer.sh` na raiz do projeto.

O servidor recebe os seguintes argumentos, sem ordem:
- `-p {porta}` (default: `3000`) - Porta do servidor
- `-b {diretoria base}` (default: `./`) - Diretoria base dos ficheiros do servidor
- `--chunkSize {tamanho}` (default: `1024`) - Tamanho do _buffer_ de leitura/escrita de *streams*

### Cliente
Para executar o cliente, basta executar o _shell script_ `myCloud.sh` na raiz do projeto.

Criar um utilizador:
- `-au {username} {password} {certificado}` - Registar utilizador para aceder ao sistema myCloud

Autenticar um utilizador 
- `-u {username} -p {password}` - Adicionar username e password de um user para aceder ao sistema myCloud

O cliente recebe os seguintes argumentos, sem ordem:
- `-a {endereco}` (default: `localhost:3000`) - Endereço e porta do servidor
- `-b {caminho}` (default: `./`) - Diretoria base do cliente
- `--keyStoreAlias {alias}` (default: `jpp`) - Nome do *alias* a usar para o _keystore_
- `--keyStorePassword {password}` (default: `123456`) - *Password* do _keystore_
- `--keyStoreAliasPassword {password}` (default: `123456`) - *Password* da _key_ *alias* do _keystore_
- `--chunkSize {tamanho}` (default: `1024`) - Tamanho do _buffer_ de leitura/escrita de *streams*
- `-d {utilizador}` (default: `São enviados para o própriio utilizador` ) - Utilizador para o qual se enviam os ficheiros

Para além destes, deve receber um e apenas um dos seguintes argumentos:
- `-c {ficheiros}+` - Cifra os ficheiros (cifra híbrida) e envia-os para o servidor.
- `-s {ficheiros}+` - Assina os ficheiros (assinatura digital) e envia-os para o servidor.
- `-e {ficheiros}+` - Assina os ficheiros (assinatura digital), cifra os ficheiros (cifra híbrida), e envia-os para o servidor. 
- `-g {ficheiros}+` - Recebe e guarda os ficheiros do servidor. Os ficheiros cifrados são decifrados, e os ficheiros assinados são verificados.
- `-x {ficheiros}+` - Elimina os ficheiros do servidor.

Onde `{ficheiros}+` é uma lista de ficheiros separados por espaço. Os ficheiros são relativos à diretoria base do cliente.

## Geração de ficheiros
Para facilitar o teste do projeto com ficheiros grandes, foi alocado o espaço necessário para o ficheiro,
seguido da escrita (*attach*) de um conteúdo conhecido para que se possa verificar que a decifra e validação estão corretas. 

Isto pode ser feito através do comando `fallocate -l {tamanho} {ficheiro}` seguido de `echo "{conteudo}" >> {ficheiro}`.

Por exemplo, para gerar um ficheiro de 3GB: `fallocate -l 3G 3GB.txt; echo "twix o cao" >> 3GB.txt`.


## Alguns extras implementados
- Suporte para ficheiros de tamanho superior a 2GB.
  - Ao usar `long` em detrimento de `int` para o envio/recepção da quantidade de *bytes* numa *stream*, é possível transferir ficheiros de tamanho igual a 2<sup>63</sup>-1 *bytes* (`8192` *PetaBytes*).
  - Como os ficheiros são transferidos para disco, o limite reside no espaço disponível no disco, geralmente muito superior à memória.
- O cliente suporta a definição do *alias* e *password* do *keystore* e *password* do *alias*.
  - Por defeito os valores são, respontivamente, `jpp`, `123456` e `123456`.
- O cliente e o servidor suportam a definição da diretoria base dos ficheiros.
  - Por defeito, a diretoria base é a diretoria atual (definido pelo `java`).
- O servidor suporta a definição da porta.
  - Por defeito, a porta é `3000`.
- O cliente e o servidor suportam a definição do tamanho do _buffer_ de leitura/escrita.
  - Por defeito, o tamanho do _buffer_ é 1024 bytes. É sugerido o tamanho de 65535 bytes por ser este o valor do _window size_ do _tcp_.
- O servidor responde a comandos/mensagens do servidor, permitindo assim escalar a quantidade de pedidos disponíveis
  - Atualmente existem os comandso `exists`, `upload`, `download`, e `delete`. O último não tem implementação no cliente.
  
## Criar keystore

```bash
username="username"
# criar keystore
keytool -genkeypair -keysize 2048 -alias $username -keyalg rsa -keystore $username.keystore -storetype PKCS12
# criar certificado
keytool -export -keystore $username.keystore -alias $username -file $username.cer
```

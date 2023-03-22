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
- `-d {diretoria}` (default: `./`) - Diretoria base dos ficheiros do servidor

### Cliente
Para executar o cliente, basta executar o _shell script_ `myCloud.sh` na raiz do projeto.

O cliente recebe os seguintes argumentos, sem ordem:
- `-a {endereco}` (default: `localhost:3000`) - Endereço e porta do servidor
- `-d {caminho}` (default: `./`) - Diretoria base do cliente
- `--keyStoreAlias {alias}` (default: `jpp`) - Nome do `alias` a usar para o _keystore_
- `--keyStorePassword {password}` (default: `123456`) - Password do _keystore_
- `--keyStoreAliasPassword {password}` (default: `123456`) - Password da _key_ `alias` do _keystore_

Para além destes, deve receber um e apenas um dos seguintes argumentos:
- `-c {ficheiros}+` - Cifra os ficheiros (cifra híbrida) e envia-os para o servidor.
- `-s {ficheiros}+` - Assina os ficheiros (assinatura digital) e envia-os para o servidor.
- `-e {ficheiros}+` - Assina os ficheiros (assinatura digital), cifra os ficheiros (cifra híbrida), e envia-os para o servidor. 
- `-g {ficheiros}+` - Recebe e guarda os ficheiros do servidor. Os ficheiros cifrados são decifrados, e os ficheiros assinados são verificados.

Onde `{ficheiros}+` é uma lista de ficheiros separados por espaço. Os ficheiros são relativos à diretoria base do cliente.
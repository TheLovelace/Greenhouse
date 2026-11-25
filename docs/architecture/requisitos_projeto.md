Atividade Integrada: Backend com Kotlin e Spring Boot, 3 Frontends com Kotlin e Tecnologias 
Associadas (Jetpack Compose, Ktor e Navigation3, Material Icon Library) e projeto IoT 
A quantidade de CRUDs do frontend deve ser proporcional a quantidade de alunos no grupo. Nesta entrega final serão 
cobrados 2 CRUDs para cada integrante de grupo. 
Parte 1 - Criar o Frontend Kotlin Multiplatform (para Android, Web e Desktop) com Jetpack Compose.  
1) Implemente o Frontend que possa interagir com as APIs criadas no backend.  
a) Crie um componente para a tela de Login contendo campos para solicitar o email e a senha, sendo que o campo senha 
deve ter os caracteres ocultados, faça um botão para logar e outro para ir à tela de registro ....................... (0,4 pontos) 
b) Crie um componente para a tela de Registro, que solicite o nome, email, senha e confirmação da senha, sendo que 
estes últimos campos devem ter os caracteres ocultados, faça um botão para registrar e outro para ir à tela de login 
.......................................................................................................................................................................... (0,4 pontos) 
c) Faça com que o Login ou Registro acessem a API REST passando os dados de usuário e senha. Em caso de sucesso, o 
frontend deverá receber o token JWT e armazená-lo em um estado compartilhado da aplicação, objeto de sessão ou 
contexto de autenticação, evitando o uso de variáveis globais soltas. Após a autenticação, o sistema deverá liberar o 
acesso à tela principal dos CRUDs. ................................................................................................................... (0,4 pontos) 
d) Faça um meio de navegação entre os CRUDs, você deve utilizar o Navigation3. Deverão haver dois níveis de navegação, 
um para navegar entre os CRUDs e outro para navegar entre tela de Formulário e Listagem ....................... (0,4 pontos) 
e) Implemente as entidades do projeto, sendo duas entidades para cada integrante existente no grupo, desde que cada 
uma delas possuam ao menos 5 campos já incluindo o id. Dentre estes campos deverá haver ao menos 2 tipos de 
dados diferentes entre estes a seguir (String, Boolean, LocalDate, LocalTime, LocalDateTime, Int, Double ou Float.) além 
do tipo do Id. .................................................................................................................................................... (0,4 pontos) 
f) 
Faça uma classe chamada RepositorioRemoto (para cada entidade), que deverá possuir os métodos e códigos que 
acionarão as funcionalidades (POST, GET, PUT, DELETE) da API REST .............................................................. (0,4 pontos) 
g) Faça um ViewModel para cada CRUD o qual deverá conter variáveis de estado para cada campo e uma variável de 
estado para guardar a lista, o ViewModel deve conter também uma função para cada ação do CRUD acionando os 
métodos correspondentes do RepositorioRemoto (da entidade)................................................................... (0,4 pontos) 
h) Faça um componente para a tela de formulário de cada CRUD onde os dados poderão ser digitados, com botões para 
as funcionalidades Gravar e Limpar Campos ................................................................................................... (0,4 pontos) 
i) 
Faça um componente para a tela de listagem de cada CRUD, de modo a mostrar um card para cada objeto existente na 
lista do ViewModel, contendo ao menos 3 campos em cada card. Coloque também ícones para apagar e editar os 
dados do card ................................................................................................................................................... (0,4 pontos) 
j) 
Funcionalidade (Apagar), ao clicar no ícone apagar do card, o App deve encaminhar uma requisição do tipo DELETE 
para o backend de forma a remover o registro, em seguida deverá invocar o carregar para trazer os dados novamente 
atualizando assim a lista com os elementos .................................................................................................... (0,4 pontos) 
k) Funcionalidade (Editar), ao clicar no ícone editar do card, o App deve mostrar o formulário com os dados do objeto  
que está sendo editado. E ao clicar no botão Gravar, o sistema deve encaminhar uma requisição do tipo PUT para o 
backend de forma a atualizar o registro com os dados existentes no formulário, em seguida deverá invocar o carregar 
para trazer os dados novamente atualizando assim a lista com os elementos ............................................... (0,4 pontos) 
Parte 2 - Backend com Kotlin + Spring Boot + Spring Security 
2) Crie o projeto base Spring Boot com Kotlin, contendo as dependências para Spring Web, Spring Data JPA, Validation, Spring 
Security e banco de dados relacional a sua escolha e faça: 
a) Implemente entidades a sua escolha, sendo que a quantidade de entidades deverá ser duas entidades para cada 
integrante, desde que cada uma delas possuam ao menos 5 campos já incluindo o id. Dentre estes campos deve haver 
ao menos 3 tipos de dados entre estes a seguir (String, Boolean, LocalDate, LocalTime, LocalDateTime, Int, Double ou 
Float)  ................................................................................................................................................................ (0,4 pontos) 
b) Crie os repositórios JPA para as entidades criadas, e coloque as annotations nestas entidades para determinar que são 
entidades e definir os ids ................................................................................................................................. (0,4 pontos) 
c) Implemente as classes de serviços para estas entidades, com lógica básica de CRUD, ou seja, contendo funções para 
criar, listar, buscar por id, atualizar e remover registros .................................................................................. (0,4 pontos) 
d) Crie os controladores REST para estas entidades, com as rotas típicas (GET, POST, PUT, DELETE), faça uma rota GET para 
pegar um elemento pelo id e outra rota GET para listar todos os elementos da entidade ............................. (0,4 pontos) 
e) Crie um controlador REST de autenticação que deve receber rotas do tipo /login e /register, este controlador deve 
validar o usuário e senha recebidos por meio do Spring Security e deve acionar a biblioteca JWT para gerar um token 
caso os dados sejam válidos, retornando este token no Response desta requisição HTTP ............................. (0,4 pontos) 
f) 
Faça uma biblioteca de utilitários para JWT de forma a gerar e validar tokens, podendo ler o usuário do token validado. 
.......................................................................................................................................................................... (0,4 pontos) 
g) Crie uma classe de configuração do Spring Security, criando Beans para passwordEncoder, AuthenticationManager, 
configuração de autorização das rotas no HttpSecurity, este último deve ter regras para permitir o acesso sem 
autenticação as rotas do tipo /login e /register e com autenticação as demais rotas ..................................... (0,4 pontos) 
h) Implemente validações de entrada com Bean Validation, como por exemplo nome obrigatório e preço maior que zero, 
sendo ao menos 2 (duas) validações para cada entidade ................................................................................ (0,4 pontos) 
i) 
Documente a API com Swagger/OpenAPI e envie uma captura de tela mostrando os endpoints funcionando, mostre as 
chamadas sendo feitas para os controllers e suas respectivas respostas.  ...................................................... (0,4 pontos) 
Parte 3 – Dispositivo IoT com Sensor e conexão via WiFi 
3) Construa um dispositivo IoT com Arduino ou ESP32 que contenha um sensor que possa ler dados do meio e transmiti-los 
via MQTT/TCP para um Broker. 
a) Construção do dispositivo IoT com Arduino ou ESP32, contendo o sensor. Forneça uma imagem do esquema feito no 
Wokwi ou no Kicad, e uma foto com boa qualidade do dispositivo construído sobre um protoboard ......... (1,2 pontos) 
b) O dispositivo IoT deve conectar a um Broker via rede Wifi e transmitir os dados coletados em cada instante para um 
tópico neste Broker. Encaminhe o código do dispositivo IoT com estas tarefas. ........................................... (0,4 pontos) 
c) Crie um serviço (Listener) no SpringBoot para assinar este tópico no Broker, guardando as informações no banco de 
dados. Lembre-se de criar a Entidade, Repositorio e Serviço para o tipo de elemento a ser gravado  ......... (0,4 pontos)
# Configurar o computador

Guia para Windows com Ubuntu 22.04 no WSL. Faça uma vez só.
Tempo estimado: 1 a 2 horas, a maior parte esperando downloads.

## Como as peças se dividem

| Ferramenta | Onde instalar | Para quê |
|---|---|---|
| Android Studio | **Windows** | Escrever e rodar o app no emulador |
| Git | Windows (vem com o Android Studio) e Ubuntu | Baixar e enviar código ao GitHub |
| Docker Desktop | **Windows**, ligado ao WSL | Rodar o Supabase no seu computador |
| Supabase CLI | **Ubuntu (WSL)** | Criar migrações e testar o banco |

O Android Studio roda no Windows porque o emulador de celular precisa do Windows.
O código fica numa pasta do Windows (`C:\dev\ludius`), que o Ubuntu também enxerga
como `/mnt/c/dev/ludius`. Assim os dois lados usam os mesmos arquivos.

## 1. Android Studio (Windows)

1. Baixe em <https://developer.android.com/studio> e instale com as opções padrão.
2. Na primeira abertura, aceite instalar o **Android SDK** e o **emulador**.
3. Em **More Actions > Virtual Device Manager**, crie um celular (ex.: Pixel 8, Android 15 ou mais novo).

## 2. Baixar o projeto

No Android Studio: **File > New > Project from Version Control**.

- URL: `https://github.com/terceirolho/ludius`
- Directory: `C:\dev\ludius`
- Clique em **Log in via GitHub** se pedir login.

Depois: **File > Open** e escolha `C:\dev\ludius\android`. Espere o Gradle sincronizar
e clique em **Run ▶**. O app abre no emulador com a tela de boas-vindas.

## 3. Git e GitHub no Ubuntu (WSL)

Abra o Ubuntu e rode, um de cada vez:

```bash
sudo apt update && sudo apt install -y git gh
git config --global user.name "Seu Nome"
git config --global user.email "seu-email-do-github"
git config --global core.autocrlf input   # evita problema de fim de linha com o Windows
gh auth login                              # escolha GitHub.com > HTTPS > Login with a web browser
cd /mnt/c/dev/ludius && git status         # deve mostrar o projeto
```

## 4. Docker Desktop (Windows)

1. Baixe em <https://www.docker.com/products/docker-desktop/> e instale.
2. Em **Settings > Resources > WSL Integration**, ligue a chave do **Ubuntu-22.04**.
3. No Ubuntu, confira: `docker ps` deve responder sem erro.

## 5. Supabase CLI (Ubuntu)

1. Abra <https://github.com/supabase/cli/releases/latest> e baixe o arquivo que termina em
   `linux_amd64.deb`.
2. No Ubuntu:

```bash
cd /mnt/c/Users/SEU_USUARIO_WINDOWS/Downloads
sudo dpkg -i supabase_*_linux_amd64.deb
supabase --version
```

3. Teste o banco do projeto:

```bash
cd /mnt/c/dev/ludius
supabase start      # a primeira vez baixa as imagens e demora uns 10 minutos
supabase test db    # deve terminar com "Result: PASS"
supabase stop
```

Com o `supabase start` ligado, o painel local abre em <http://127.0.0.1:54323>.

## Dia a dia

```bash
git switch main && git pull          # pega a versão mais nova
git switch -c minha-mudanca          # cria um ramo para trabalhar
# ... edita ...
git add -A && git commit -m "Explica a mudança"
git push -u origin minha-mudanca     # depois abre o pull request no GitHub
```

Se algo der errado em qualquer passo, copie a mensagem de erro e me mande.

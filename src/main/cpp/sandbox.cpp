#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <signal.h>
#include <pthread.h>
#include <sys/wait.h>
#include <sys/resource.h>
#include <pthread.h>

const pid_t SANDBOX_UID = 1111;
const pid_t SANDBOX_GID = 1111;

unsigned long parse_long(char *str) {
    unsigned long x = 0;
    for (char *p = str; *p; p++)
        x = x * 10 + *p - '0';
    return x;
}

pid_t pid;
long time_limit_to_watch;
bool time_limit_exceeded_killed;

void *watcher_thread(void *arg) {
    sleep((unsigned int) time_limit_to_watch);
    kill(pid, SIGKILL);
    time_limit_exceeded_killed = true;
    return arg; // Avoid 'parameter set but not used' warning
}

int main(int argc, char **argv) {
    if (argc != 8 + 1) {
        fprintf(stderr, "Error: need 8 arguments\n");
        fprintf(stderr,
                "Usage: %s cmd file_stdout file_stderr time_limit memory_limit stack_limit output_limit file_result\n",
                argv[0]);
        return 1;
    }

//    if (getuid() != 0) {
//        fprintf(stderr, "Error: need root privileges\n");
//        return 1;
//    }

    char *cmd = argv[1],
            *file_stdout = argv[2],
            *file_stderr = argv[3],
            *file_result = argv[8];
    long time_limit = parse_long(argv[4]),
            memory_limit = parse_long(argv[5]),
            stack_limit = parse_long(argv[6]),
            output_limit = parse_long(argv[7]);

    time_limit_to_watch = time_limit;

#ifdef LOG
    printf("Program: %s\n", cmd);
    printf("Standard output file: %s\n", file_stdout);
    printf("Standard error file: %s\n", file_stderr);
    printf("Output limit (bytes): %lu\n", output_limit);
    printf("Result file: %s\n", file_result);
#endif

    pid = fork();
    if (pid != 0) {
        // Parent process

        FILE *fresult = fopen(file_result, "w");
        if (!fresult) {
            printf("Failed to open result file '%s'.", file_result);
            return -1;
        }

        if (time_limit) {
            pthread_t thread_id;
            pthread_create(&thread_id, NULL, &watcher_thread, NULL);
        }

        struct rusage usage;
        int status;
        if (wait4(pid, &status, 0, &usage) == -1) {
            fprintf(fresult, "Runtime Error\nwait4() = -1\n0\n0\n");
            return 0;
        }

        if (WIFEXITED(status)) {
            // Not signaled - exited normally
            if (WEXITSTATUS(status) != 0)
                fprintf(fresult, "Runtime Error\nWIFEXITED - WEXITSTATUS() = %d\n", WEXITSTATUS(status));
            else
                fprintf(fresult, "Exited Normally\nWIFEXITED - WEXITSTATUS() = %d\n", WEXITSTATUS(status));
        } else {
            // Signaled
            int sig = WTERMSIG(status);
            if (sig == SIGXCPU || usage.ru_utime.tv_sec > time_limit || time_limit_exceeded_killed)
                fprintf(fresult, "Time Limit Exceeded\nWEXITSTATUS() = %d, WTERMSIG() = %d (%s)\n",
                        WEXITSTATUS(status), sig, strsignal(sig));
            else if (sig == SIGXFSZ)
                fprintf(fresult, "Output Limit Exceeded\nWEXITSTATUS() = %d, WTERMSIG() = %d (%s)\n",
                        WEXITSTATUS(status), sig, strsignal(sig));
            else if (usage.ru_maxrss > memory_limit)
                fprintf(fresult, "Memory Limit Exceeded\nWEXITSTATUS() = %d, WTERMSIG() = %d (%s)\n",
                        WEXITSTATUS(status), sig, strsignal(sig));
            else
                fprintf(fresult, "Runtime Error\nWEXITSTATUS() = %d, WTERMSIG() = %d (%s)\n",
                        WEXITSTATUS(status), sig, strsignal(sig));
        }

#ifdef LOG
        printf("memory_usage = %ld\n", usage.ru_maxrss);
#endif
        if (time_limit_exceeded_killed)
            fprintf(fresult, "%ld\n", time_limit_to_watch * 1000000);
        else
            fprintf(fresult, "%ld\n", (long) (usage.ru_utime.tv_sec * 1000000 + usage.ru_utime.tv_usec));
        fprintf(fresult, "%ld\n", usage.ru_maxrss);

        fclose(fresult);
    } else {
#ifdef LOG
        puts("Entered child process.");
#endif

        // Child process

        if (time_limit) {
            struct rlimit lim;
            lim.rlim_cur = (rlim_t) (time_limit);
            lim.rlim_max = (rlim_t) (time_limit);
            setrlimit(RLIMIT_CPU, &lim);
        }

        if (memory_limit) {
            struct rlimit lim;
            lim.rlim_cur = (rlim_t) ((memory_limit) * 1024);
            lim.rlim_max = (rlim_t) ((memory_limit) * 1024);
            setrlimit(RLIMIT_AS, &lim);
            if (stack_limit)
                setrlimit(RLIMIT_STACK, &lim);
        }

        if (output_limit) {
            struct rlimit lim;
            lim.rlim_cur = (rlim_t) output_limit;
            lim.rlim_max = (rlim_t) output_limit;
            setrlimit(RLIMIT_FSIZE, &lim);
        }

#ifdef LOG
        puts("Entering target cmd...");
#endif

        chdir("/sandbox");

        setuid(SANDBOX_UID);
        setgid(SANDBOX_GID);

        freopen(file_stdout, "w", stdout);
        freopen(file_stderr, "w", stderr);

        execlp(cmd, NULL);
    }

    return 0;
}
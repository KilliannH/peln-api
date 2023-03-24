import argparse
import time
import torch
import math
from werkzeug.wrappers import Request, Response
import os
from transformers import AutoTokenizer, AutoConfig, AutoModelForCausalLM
from dotenv import load_dotenv, find_dotenv

load_dotenv(find_dotenv())

API_KEY = os.environ.get("API_KEY")


def get_args():
    parser = argparse.ArgumentParser()
    parser.add_argument("--batch_size", default=1, type=int, help="batch size")
    parser.add_argument("--greedy", action="store_true")
    parser.add_argument("--top-k", type=int, default=0)
    parser.add_argument("--top-p", type=float, default=0.)

    return parser.parse_args()


def prepare_generate(input_sentences, max_new_tokens):
    print(f"*** Starting to generate {max_new_tokens} tokens with bs={args.batch_size}")

    if args.batch_size > len(input_sentences):
        # dynamically extend to support larger bs by repetition
        input_sentences *= math.ceil(args.batch_size / len(input_sentences))

    inputs = input_sentences[:args.batch_size]
    return inputs


def generate(inputs, generate_kwargs):
    """ returns a list of zipped inputs, outputs and number of new tokens """

    input_tokens = tokenizer.batch_encode_plus(inputs, return_tensors="pt", padding=True)
    for t in input_tokens:
        if torch.is_tensor(input_tokens[t]):
            input_tokens[t] = input_tokens[t].to(device)

    outputs = model.generate(**input_tokens, **generate_kwargs)

    input_tokens_lengths = [x.shape[0] for x in input_tokens.input_ids]
    output_tokens_lengths = [x.shape[0] for x in outputs]

    total_new_tokens = [o - i for i, o in zip(input_tokens_lengths, output_tokens_lengths)]
    outputs = tokenizer.batch_decode(outputs, skip_special_tokens=True)

    return zip(inputs, outputs, total_new_tokens)


t_start = time.time()

device = "cpu"

args = get_args()

model_name = "bigscience/bloom-1b1"

print(f"Loading model {model_name}")

tokenizer = AutoTokenizer.from_pretrained(model_name)

dtype = torch.bfloat16

model = AutoModelForCausalLM.from_pretrained(
    model_name,
    device_map="auto",
    torch_dtype=dtype,
)

print("Warm up")
# Generate
# warmup is a must if measuring speed as it's when all the optimizations are performed
# e.g. on 8x80 a100 the first pass of 100 tokens takes 23sec, and the next one is 4secs
warm_tokens = 10
inputs = prepare_generate(["The SQL command to extract all the users whose name starts with A is: "], warm_tokens)
# Generate kwargs
generate_kwargs = dict(max_new_tokens=warm_tokens, do_sample=False)
_ = generate(inputs, generate_kwargs)

port = 9001
host = '0.0.0.0'


@Request.application
def application(request):
    if 'Authorization' not in request.headers:
        return Response('Unauthorized', status=403)

    if API_KEY != request.headers['Authorization']:
        return Response('Unauthorized', status=403)

    if request.method == 'POST' and request.path == '/test':
        # Parse params
        # {"inputs": "..", {parameters: "max_new_tokens": 10, "..."}}
        json_values = request.get_json()
        print("body", json_values)

        do_sample = None
        early_stopping = None
        length_penalty = None
        max_new_tokens = 1
        seed = None
        top_p = None
        inp = None

        if 'do_sample' in json_values['parameters']:
            do_sample = json_values['parameters']['do_sample']

        if 'early_stopping' in json_values['parameters']:
            early_stopping = json_values['parameters']['early_stopping']

        if 'length_penalty' in json_values['parameters']:
            length_penalty = json_values['parameters']['length_penalty']

        if 'max_new_tokens' in json_values['parameters']:
            max_new_tokens = json_values['parameters']['max_new_tokens']

        if 'seed' in json_values['parameters']:
            seed = json_values['parameters']['seed']

        if 'top_p' in json_values['parameters']:
            top_p = json_values['parameters']['top_p']

        if json_values['inputs']:
            inp = json_values['inputs']

        # Prepare generate
        inputs = prepare_generate([inp], max_new_tokens)

        # Generate kwargs
        generate_kwargs = dict(max_new_tokens=max_new_tokens, do_sample=False)
        print(f"Generate args {generate_kwargs}")

        # Generate
        t_generate_start = time.time()
        generated = generate(inputs, generate_kwargs)
        t_generate_span = time.time() - t_generate_start
        print("Time to generate : " + str(t_generate_span))

        result = ""
        for i, o, _ in generated:
            # result += f"in={i}\nout={o}"
            result += o

        return Response(response='{"generated_text": ' + result + '}', content_type="application/json")
    else:
        return Response('Bad Request', status=400)


if __name__ == '__main__':
    from werkzeug.serving import run_simple

    run_simple(host, port, application)

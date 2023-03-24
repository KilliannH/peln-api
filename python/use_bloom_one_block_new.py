import os
import torch
import torch.nn as nn
from collections import OrderedDict
import time

def get_state_dict(shard_num, prefix=None):
    d = torch.load(os.path.join(model_path, f"pytorch_model_{shard_num:05d}-of-00072.bin"))
    return d if prefix is None else OrderedDict((k.replace(prefix, ''), v) for k, v in d.items())


from transformers import AutoTokenizer, AutoModelForCausalLM, BloomConfig
from transformers.models.bloom.modeling_bloom import BloomBlock, build_alibi_tensor

model_path = "/home/projets/bloom"  # replace with your local folder path
config = BloomConfig.from_pretrained(model_path)
tokenizer = AutoTokenizer.from_pretrained(model_path)
device = 'cpu'


def load_embeddings():
    state_dict = get_state_dict(shard_num=1, prefix="word_embeddings_layernorm.")
    embeddings = nn.Embedding.from_pretrained(state_dict.pop('word_embeddings.weight'))
    lnorm = nn.LayerNorm(config.hidden_size, eps=config.layer_norm_epsilon, dtype=torch.bfloat16)
    lnorm.load_state_dict(state_dict)
    return embeddings.to(device), lnorm.to(device)


def load_causal_lm_head():
    linear = nn.utils.skip_init(
        nn.Linear, config.hidden_size, config.vocab_size, bias=False, dtype=torch.bfloat16)
    linear.load_state_dict(get_state_dict(shard_num=1, prefix="word_embeddings."), strict=False)
    return linear.bfloat16().to(device)


def load_block(block_num):
    print("on charge le bloc :", block_num)
    block_obj = BloomBlock(config, layer_number=block_num).bfloat16()
    block_obj.load_state_dict(get_state_dict(shard_num=block_num + 2, prefix=f"h.{block_num}."))
    return block_obj


def load_final_lnorm():
    final_lnorm = nn.LayerNorm(config.hidden_size, eps=config.layer_norm_epsilon, dtype=torch.bfloat16)
    final_lnorm.load_state_dict(get_state_dict(shard_num=72, prefix="ln_f."))
    final_lnorm.to(device)
    return final_lnorm


# Load all modules to RAM and GPU (except the blocks, which are only loaded to RAM)
time_start = time.time()
blocks = []
# for block_num in range(70):
#    time_step_start = time.time()
#    blocks.append(load_block(block_num))
#    print("time to compute the block number " + str(block_num) + " : ", time.time() - time_step_start)
# blocks = [load_block(block_num) for block_num in range(70)]
embeddings, emb_lnorm = load_embeddings()
lm_head = load_causal_lm_head()
final_lnorm = load_final_lnorm()
time_end = time.time()
print("Time to load all the blocks: ", time_end - time_start)


def loadOnce():
    trained_model = torch.load("iris-model-full.pth")
    return trained_model

def loadAllBlocks():
    for block_num in range(70):
        time_step_start = time.time()
        block = load_block(block_num)
        print("time to compute the block number " + str(block_num) + " : ", time.time() - time_step_start)

        print(".", end='')
        blocks.append(block)


def forward(input_ids):
    # 1. Create attention mask and position encodings
    attention_mask = torch.ones(len(input_ids)).unsqueeze(0).bfloat16().to(device)
    alibi = build_alibi_tensor(input_ids.shape[1], config.num_attention_heads,
                               torch.bfloat16).to(device)
    # 2. Use word embeddings and associated lnorm
    hidden_states = emb_lnorm(embeddings(input_ids))

    # 3. Use the BLOOM blocks sequentially

    for test in tests:
        block_gpu = test.to(device)  # Move single block to GPU
        hidden_states = block_gpu(hidden_states, attention_mask=attention_mask, alibi=alibi)[0]
        print(".", end='')

    hidden_states = final_lnorm(hidden_states)

    # 4. Use language model head
    logits = lm_head(hidden_states)

    # 5. Compute next token
    return torch.argmax(logits[:, -1, :], dim=-1)


# we actually don't care about those 3 lines as it's for inference.
# input_sentence = "1+1= "
# input_ids = tokenizer.encode(input_sentence, return_tensors='pt').to(device)
# max_tokens = 1

print("loadOnce")
time_start = time.time()
tests = loadOnce()
time_end = time.time()
print("Time to load model: " + str(time_end - time_start)) # 6 minutes
# torch.save(blocks, "iris-model-full.pth")
# store the all trained stuff

input_sentence = "The SQL command to extract all the users whose name starts with A is: "
input_ids = tokenizer.encode(input_sentence, return_tensors='pt').to(device)
max_tokens = 10

print("forward")
time_start = time.time()
for i in range(max_tokens):
    print(f"Token {i + 1} ", end='')
    new_id = forward(input_ids)
    input_ids = torch.cat([input_ids, new_id.unsqueeze(-1)], dim=-1)
    print(tokenizer.decode(new_id))
    time_end = time.time()
    print("Time to compute a token: "  , time_end - time_start)
    time_start = time_end

print(tokenizer.decode(input_ids.squeeze(), skip_special_tokens=True))

'''
print("forward")
time_start = time.time()
for i in range(max_tokens):
    print(f"Token {i + 1} ", end='')
    new_id = forward(input_ids)
    input_ids = torch.cat([input_ids, new_id.unsqueeze(-1)], dim=-1)
    print(tokenizer.decode(new_id))
    time_end = time.time()
    print("Time to compute a token: "  , time_end - time_start)
    time_start = time_end

print(tokenizer.decode(input_ids.squeeze(), skip_special_tokens=True))


input_sentence = "The SQL command to extract all the users whose name starts with A is: "
input_ids = tokenizer.encode(input_sentence, return_tensors='pt').to(device)
max_tokens = 10

print("forward")
time_start = time.time()
for i in range(max_tokens):
    print(f"Token {i + 1} ", end='')
    new_id = forward(input_ids)
    input_ids = torch.cat([input_ids, new_id.unsqueeze(-1)], dim=-1)
    print(tokenizer.decode(new_id))
    time_end = time.time()
    print("Time to compute a token: "  , time_end - time_start)
    time_start = time_end

print(tokenizer.decode(input_ids.squeeze(), skip_special_tokens=True))
'''

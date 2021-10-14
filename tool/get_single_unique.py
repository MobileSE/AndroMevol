import os
import re


class API_Project:
    def __init__(self, class_name, rtn_type, method_name, parameters, api, pkg_level):
        self.class_name = class_name
        self.rtn_type = rtn_type
        self.method_name = method_name
        self.parameters = parameters
        self.para_num = len(parameters)
        self.api = api
        self.class_name_list = class_name.split('.')
        self.pkg_level = pkg_level
        self.len_pkg = len(self.class_name_list)


def save_lst(lst, filename):
    with open(filename, "w") as fw:
        for item in lst:
            fw.write(item)
            fw.write("\n")


def load_file(filename):
    lst = []
    all = []
    res = {}
    with open(filename, "r") as fr:
        lines = fr.readlines()
        for line in lines:
            s = line.split(":<")
            api_sig = s[0].strip()

            modifiers = re.split('[ :]', s[1].strip("<>"))

            res[api_sig] = list(set(modifiers))
            lst.append(api_sig)
            all.append(line.strip())
    return res, lst, all


def load_file_neat(filename):
    res = {}
    with open(filename, "r") as fr:
        lines = fr.readlines()
        for line in lines:
            s = line.strip()
            res[s] = 1
    return res


def filter_methods(FrameworkOriginalFile, FrameworkFilteredFile):
    res, lst, all = load_file(FrameworkOriginalFile)
    new_lst = []
    new_lst_2 = []

    for i in range(len(lst)):
        item = lst[i]
        if "'" in item:
            continue
        match = re.search(r'<(\S+):\s(\S+)\s(\S+)\((.*)\)>', item)
        if match:
            class_name = match.group(1)
            rtn_type = match.group(2)
            method_name = match.group(3)
            parameters = match.group(4)

            match1 = re.search(r'\S+\.\d+$', class_name)
            if match1:
                # print(all[i])
                continue

            hash = ".".join(class_name.split(".")[:3])
            if hash == "com.android.framework":
                # print(all[i])
                continue

            match2 = re.search(r'\S+\.\d+$', method_name)
            if match2:
                # print(all[i])
                continue

            if "." in method_name:  # 可以删
                # print(lst[i])
                continue

            # if ".stub." in class_name:
            #     # print(lst[i])
            #     continue

            match4 = re.search(r'\.V\d+_\d+\.', class_name)
            if match4:
                # print(all[i])
                continue

            flag1 = 0
            for paramter in parameters:
                match3 = re.search(r'\S+\.\d+$', paramter.strip())
                if match3:
                    flag1 = 1
                    break
            if flag1 == 1:
                continue

        modifiers = res[item]
        flag = 0
        for m in modifiers:
            if m != "":
                flag = 1
            if m.strip() == "private" or m.strip() == "default":
                flag = 0
                break

        if flag == 0:
            # print(all[i])
            continue

        new_lst.append(all[i])
        new_lst_2.append(lst[i])

    save_lst(new_lst_2, FrameworkFilteredFile)
    print("framework original: ", len(all), " filtered: ", len(new_lst))

def filter_fields(FrameworkOriginalFile, FrameworkFilteredFile):
    res, lst, all = load_file(FrameworkOriginalFile)
    new_lst = []
    new_lst_2 = []

    for i in range(len(lst)):
        item = lst[i]
        if "'" in item:
            continue
        match = re.search(r'<(\S+):\s(\S+)\s(\S+)>', item)
        if match:
            class_name = match.group(1)
            rtn_type = match.group(2)
            field_name = match.group(3)

            match1 = re.search(r'\S+\.\d+$', class_name)
            if match1:
                # print(all[i])
                continue

            match4 = re.search(r'\.V\d+_\d+\.', class_name)
            if match4:
                # print(all[i])
                continue

            hash = ".".join(class_name.split(".")[:3])
            if hash == "com.android.framework":
                continue

            match2 = re.search(r'\S+\.\d+$', field_name)
            if match2:
                # print(all[i])
                continue

            if "." in field_name:  # 可以删
                # print(lst[i])
                continue

        modifiers = res[item]
        flag = 0
        for m in modifiers:
            if m != "":
                flag = 1
            if m.strip() == "private":
                flag = 0
                break

        if flag == 0:
            # print(all[i])
            continue

        new_lst.append(all[i])
        new_lst_2.append(lst[i])

    save_lst(new_lst_2, FrameworkFilteredFile)
    print("framework original: ", len(all), " filtered: ", len(new_lst))

def get_by_pkg(pkg_level, METHODS_or_FILEDS, all_discard_pkg, sourcecode, SAVE_NEW):

    # remove discard
    sourcecodediscard_pkg = load_file_neat(all_discard_pkg)

    res = load_file_neat(sourcecode)

    new = []

    for item in res:

        if METHODS_or_FILEDS == "M":
            match = re.search(r'<(\S+):\s(\S+)\s(\S+)\((.*)\)>', item)
        else:
            match = re.search(r'<(\S+):\s(\S+)\s(\S+)>', item)

        if match:
            class_name = match.group(1)
            class_name_len = len(class_name.split("."))
            if class_name_len >= pkg_level:
                pkg_name = ".".join(class_name.split(".")[:pkg_level])
            else:
                pkg_name = class_name
            if pkg_name not in sourcecodediscard_pkg:
                new.append(item)

    print(len(res), len(new))

    save_lst(new, SAVE_NEW)


if __name__ == '__main__':

    # run miui and official-framework-30

    level = 30
    pkg_level = 4
    types = ["M", "F"]
    for typ in types:
        METHODS_or_FILEDS = typ  # "M" or "F"

        # input field discard
        all_discard_pkg = "filter_pkg.txt"
        print("pkg length: ", pkg_level)

        devices = ["huawei", "miui", "oneplus", "official", "oppo", "samsung"]
        for device in devices:
            for d_level in range(19, 31):
                # input files
                MiuiMethodFramework = "../res/{}/framework-{}/methods.txt".format(device, d_level)
                if not os.path.exists(MiuiMethodFramework):
                    continue
                MiuiMethodFrameworkFiltered = "../res/{}/framework-{}/methods-filtered.txt".format(device, d_level)

                MiuiFieldFramework = "../res/{}/framework-{}/fields.txt".format(device, d_level)
                MiuiFieldFrameworkFiltered = "../res/{}/framework-{}/fields-filtered.txt".format(device, d_level)


                # output files
                OUTPUT_ROOT = "../res/{}/framework-{}".format(device, d_level)
                if not os.path.exists(OUTPUT_ROOT):
                    os.mkdir(OUTPUT_ROOT)
                SAVE_Methods_unique = os.path.join(OUTPUT_ROOT, "methods_unique.txt")
                SAVE_Fields_unique = os.path.join(OUTPUT_ROOT, "fields_unique.txt")

                # Framework filter first
                if METHODS_or_FILEDS == "M":
                    if device != 'official':
                        filter_methods(MiuiMethodFramework, MiuiMethodFrameworkFiltered)
                    else:
                        src_path = "../res/{}/framework-{}/methods.txt".format(device, d_level)
                        dest_path = "../res/{}/framework-{}/methods-filtered.txt".format(device, d_level)
                        os.popen('cp ' + src_path + ' ' + dest_path).read()
                else:
                    if device != 'official':
                        filter_fields(MiuiFieldFramework, MiuiFieldFrameworkFiltered)
                    else:
                        src_path = "../res/{}/framework-{}/fields.txt".format(device, d_level)
                        dest_path = "../res/{}/framework-{}/fields-filtered.txt".format(device, d_level)
                        os.popen('cp ' + src_path + ' ' + dest_path).read()


                # select
                get_by_pkg(pkg_level, METHODS_or_FILEDS, all_discard_pkg, MiuiMethodFrameworkFiltered, SAVE_Methods_unique)
